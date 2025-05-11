package com.gabbasov.meterscan.scan.domain

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Детектор цифр на счетчике с использованием TensorFlow Lite
 */
class MeterDetector(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String,
    private val detectorListener: DetectorListener
) {
    private var interpreter: Interpreter
    private var labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    // Буфер для накопления результатов распознавания
    private val recognitionHistory = mutableListOf<List<DigitBox>>()
    private val historyMaxSize = 5 // Размер истории для сглаживания результатов

    init {
        Log.d("MeterDetector", "Initializing detector with model: $modelPath, labels: $labelPath")

        val compatList = CompatibilityList()

        val options = Interpreter.Options().apply {
            val delegateOptions = compatList.bestOptionsForThisDevice
            this.addDelegate(GpuDelegate(delegateOptions))
            this.setNumThreads(12) // Установка 12 потоков для повышения производительности
            this.setUseNNAPI(true) // Включение NNAPI для аппаратного ускорения
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)
        Log.d("MeterDetector", "Interpreter initialized")

        val inputShape = interpreter.getInputTensor(0)?.shape()
        val outputShape = interpreter.getOutputTensor(0)?.shape()

        if (inputShape != null) {
            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]

            // Если входная форма в формате [1, 3, ..., ...]
            if (inputShape[1] == 3) {
                tensorWidth = inputShape[2]
                tensorHeight = inputShape[3]
            }
            Log.d("MeterDetector", "Input shape: ${inputShape.contentToString()}, tensor size: $tensorWidth x $tensorHeight")
        }

        if (outputShape != null) {
            numChannel = outputShape[1]
            numElements = outputShape[2]
            Log.d("MeterDetector", "Output shape: ${outputShape.contentToString()}, numChannel: $numChannel, numElements: $numElements")
        }

        try {
            val inputStream: InputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
            Log.d("MeterDetector", "Loaded ${labels.size} labels: $labels")
        } catch (e: IOException) {
            Log.e("MeterDetector", "Failed to load labels: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun close() {
        interpreter.close()
    }

    fun detect(frame: Bitmap) {
        Log.d("MeterDetector", "Detecting on bitmap: ${frame.width}x${frame.height}")

        if (tensorWidth == 0) return
        if (tensorHeight == 0) return
        if (numChannel == 0) return
        if (numElements == 0) return

        var inferenceTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer
        Log.d("MeterDetector", "Tensor image created and processed")

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter.run(imageBuffer, output.buffer)
        Log.d("MeterDetector", "Inference completed, output size: ${output.floatArray.size}")

        // Получаем исходные результаты распознавания
        val initialDetections = processOutput(output.floatArray)

        // Применяем улучшенную фильтрацию
        val filteredDetections = filterDetectedDigits(initialDetections)

        // Добавляем результат в историю распознаваний
        recognitionHistory.add(filteredDetections)
        if (recognitionHistory.size > historyMaxSize) {
            recognitionHistory.removeAt(0)
        }

        // Получаем сглаженный результат на основе истории
        val stabilizedDetections = stabilizeDetections()

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        Log.d("MeterDetector", "Detection completed in $inferenceTime ms, found ${stabilizedDetections.size} digits")

        if (stabilizedDetections.isEmpty()) {
            detectorListener.onEmptyDetect()
            return
        }

        detectorListener.onDetect(stabilizedDetections, inferenceTime)
    }

    /**
     * Обрабатывает результат распознавания, сортирует цифры по X-координате и фильтрует шум
     */
    private fun processOutput(array: FloatArray): List<DigitBox> {
        Log.d("MeterDetector", "Processing output array of size ${array.size}, labels count: ${labels.size}")

        if (labels.isEmpty()) {
            Log.e("MeterDetector", "Labels list is empty. Check if labels file exists and readable.")
            return emptyList()
        }

        val digitBoxes = mutableListOf<DigitBox>()

        for (c in 0 until numElements) {
            var maxConf = CONFIDENCE_THRESHOLD
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j

            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val digitValue = labels[maxIdx]
                val cx = array[c] // центр X
                val cy = array[c + numElements] // центр Y
                val w = array[c + numElements * 2] // ширина
                val h = array[c + numElements * 3] // высота
                val x1 = cx - (w/2F) // левая граница
                val y1 = cy - (h/2F) // верхняя граница
                val x2 = cx + (w/2F) // правая граница
                val y2 = cy + (h/2F) // нижняя граница

                Log.d("MeterDetector", "Detected digit: $digitValue at ($cx,$cy) with confidence $maxConf")

                // Проверка, что бокс находится в пределах изображения
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                digitBoxes.add(
                    DigitBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        confidence = maxConf, digit = digitValue
                    )
                )
            }
        }

        Log.d("MeterDetector", "Initial digit boxes count: ${digitBoxes.size}")

        if (digitBoxes.isEmpty()) return emptyList()

        // Применяем NMS для устранения дубликатов
        val filteredBoxes = applyNMS(digitBoxes)
        Log.d("MeterDetector", "After NMS: ${filteredBoxes.size} boxes")

        // Фильтруем шум - убираем цифры, которые находятся далеко от остальных
        val result = filterOutliers(filteredBoxes)
        Log.d("MeterDetector", "After outlier filtering: ${result.size} boxes, sorted: ${result.sortedBy { it.cx }.map { it.digit }}")

        return result
    }

    /**
     * Применяет улучшенную фильтрацию результатов распознавания
     */
    private fun filterDetectedDigits(digitBoxes: List<DigitBox>): List<DigitBox> {
        if (digitBoxes.size <= 1) return digitBoxes

        // 1. Сортируем по X-координате
        val sortedBoxes = digitBoxes.sortedBy { it.cx }

        // 2. Анализируем геометрические характеристики
        val avgY = sortedBoxes.map { it.cy }.average().toFloat()
        val avgHeight = sortedBoxes.map { it.h }.average().toFloat()
        val avgWidth = sortedBoxes.map { it.w }.average().toFloat()

        // 3. Отфильтровываем цифры по вертикальному выравниванию
        val alignedByY = sortedBoxes.filter {
            abs(it.cy - avgY) < avgHeight * 0.3
        }

        if (alignedByY.size <= 1) return alignedByY

        // 4. Анализируем расстояния между цифрами
        val distances = (0 until alignedByY.size - 1).map { i ->
            alignedByY[i + 1].cx - alignedByY[i].cx
        }

        // 5. Находим медианное расстояние между цифрами
        val medianDistance = distances.sorted()[distances.size / 2]

        // 6. Отфильтровываем аномально расположенные цифры
        val result = mutableListOf<DigitBox>()
        result.add(alignedByY[0])

        for (i in 1 until alignedByY.size) {
            val prevBox = alignedByY[i - 1]
            val currBox = alignedByY[i]
            val distance = currBox.cx - prevBox.cx

            // 6.1 Проверяем, что расстояние между цифрами в разумных пределах
            val minValidDistance = avgWidth * 0.5
            val maxValidDistance = avgWidth * 3.0

            if (distance in minValidDistance..maxValidDistance) {
                result.add(currBox)
            } else if (currBox.confidence > HIGH_CONFIDENCE_THRESHOLD) {
                // Если уверенность очень высокая, добавляем несмотря на расстояние
                result.add(currBox)
            }
        }

        // 7. Отфильтровываем соседние дубликаты
        return filterDuplicatesInSequence(result)
    }

    /**
     * Удаляет дублирующиеся цифры, которые могут быть распознаны на одном и том же месте
     */
    private fun filterDuplicatesInSequence(digitBoxes: List<DigitBox>): List<DigitBox> {
        if (digitBoxes.size <= 1) return digitBoxes

        val result = mutableListOf<DigitBox>()
        result.add(digitBoxes[0])

        for (i in 1 until digitBoxes.size) {
            val prevBox = digitBoxes[i - 1]
            val currBox = digitBoxes[i]

            // Проверяем, не является ли текущая цифра дубликатом предыдущей
            val overlapRatio = calculateOverlap(prevBox, currBox)

            if (overlapRatio < 0.5) { // Если перекрытие меньше 50%
                result.add(currBox)
            } else {
                // Если значительное перекрытие, оставляем цифру с большей уверенностью
                if (currBox.confidence > prevBox.confidence) {
                    result[result.size - 1] = currBox
                }
            }
        }

        return result
    }

    /**
     * Вычисляет степень перекрытия двух боксов
     */
    private fun calculateOverlap(box1: DigitBox, box2: DigitBox): Float {
        val x1 = max(box1.x1, box2.x1)
        val y1 = max(box1.y1, box2.y1)
        val x2 = min(box1.x2, box2.x2)
        val y2 = min(box1.y2, box2.y2)

        // Проверяем, есть ли перекрытие
        if (x1 >= x2 || y1 >= y2) return 0f

        val intersection = (x2 - x1) * (y2 - y1)
        val area1 = box1.w * box1.h
        val area2 = box2.w * box2.h

        return intersection / min(area1, area2)
    }

    /**
     * Стабилизирует результаты на основе истории распознаваний
     */
    private fun stabilizeDetections(): List<DigitBox> {
        if (recognitionHistory.isEmpty()) return emptyList()

        // Подсчитываем частоту появления цифр в каждой позиции
        val digitFrequency = mutableMapOf<String, MutableMap<Int, Int>>()
        val positionMapping = mutableMapOf<String, MutableMap<Int, DigitBox>>()

        // Получаем уникальные последовательности цифр из истории
        recognitionHistory.forEach { detection ->
            if (detection.isEmpty()) return@forEach

            val sortedDigits = detection.sortedBy { it.cx }
            val reading = sortedDigits.joinToString("") { it.digit }

            if (!digitFrequency.containsKey(reading)) {
                digitFrequency[reading] = mutableMapOf()
                positionMapping[reading] = mutableMapOf()
            }

            // Для каждой цифры в последовательности обновляем частоту и сохраняем позицию
            sortedDigits.forEachIndexed { index, box ->
                digitFrequency[reading]?.let { map ->
                    map[index] = (map[index] ?: 0) + 1
                }
                positionMapping[reading]?.let { map ->
                    map[index] = box
                }
            }
        }

        // Находим наиболее часто встречающуюся последовательность
        val mostFrequentReading = digitFrequency.maxByOrNull { entry ->
            entry.value.values.sum()
        }?.key ?: return emptyList()

        // Возвращаем боксы для этой последовательности
        return positionMapping[mostFrequentReading]?.values?.toList() ?: emptyList()
    }

    /**
     * Применяет алгоритм Non-Maximum Suppression для устранения перекрывающихся боксов
     */
    private fun applyNMS(boxes: List<DigitBox>): MutableList<DigitBox> {
        val sortedBoxes = boxes.sortedByDescending { it.confidence }.toMutableList()
        val selectedBoxes = mutableListOf<DigitBox>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    /**
     * Фильтрует выбросы - цифры, которые находятся далеко от основной группы
     */
    private fun filterOutliers(boxes: List<DigitBox>): List<DigitBox> {
        if (boxes.size <= 1) return boxes

        // Сортируем боксы по X-координате
        val sortedBoxes = boxes.sortedBy { it.cx }

        // Находим среднюю Y-координату и высоту цифр
        val avgY = sortedBoxes.map { it.cy }.average().toFloat()
        val avgHeight = sortedBoxes.map { it.h }.average().toFloat()

        // Фильтруем боксы, которые находятся слишком далеко по Y от основной линии
        val filteredByY = sortedBoxes.filter {
            abs(it.cy - avgY) < avgHeight * Y_TOLERANCE_FACTOR
        }

        // Если осталось мало боксов, возвращаем их
        if (filteredByY.size <= 1) return filteredByY

        // Анализируем расстояния между соседними цифрами по X
        val distances = mutableListOf<Float>()
        for (i in 0 until filteredByY.size - 1) {
            val distance = filteredByY[i + 1].cx - filteredByY[i].cx
            distances.add(distance)
        }

        // Находим медианное расстояние между цифрами
        val medianDistance = distances.sorted()[distances.size / 2]

        // Фильтруем боксы, между которыми слишком большие расстояния
        val result = mutableListOf<DigitBox>()
        result.add(filteredByY[0])

        for (i in 1 until filteredByY.size) {
            val prevBox = filteredByY[i - 1]
            val currBox = filteredByY[i]
            val distance = currBox.cx - prevBox.cx

            // Если расстояние в пределах допустимого, добавляем бокс
            if (distance <= medianDistance * X_TOLERANCE_FACTOR) {
                result.add(currBox)
            } else {
                // Проверяем уверенность распознавания
                if (currBox.confidence > HIGH_CONFIDENCE_THRESHOLD) {
                    result.add(currBox)
                }
            }
        }

        return result
    }

    /**
     * Вычисляет IoU (Intersection over Union) для двух боксов
     */
    private fun calculateIoU(box1: DigitBox, box2: DigitBox): Float {
        val x1 = max(box1.x1, box2.x1)
        val y1 = max(box1.y1, box2.y1)
        val x2 = min(box1.x2, box2.x2)
        val y2 = min(box1.y2, box2.y2)
        val intersectionArea = max(0F, x2 - x1) * max(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    /**
     * Слушатель событий детектора
     */
    interface DetectorListener {
        fun onEmptyDetect()
        fun onDetect(digitBoxes: List<DigitBox>, inferenceTime: Long)
    }

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.5F
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.95F  // Порог высокой уверенности
        private const val IOU_THRESHOLD = 0.5F
        private const val Y_TOLERANCE_FACTOR = 0.5F  // Фактор допуска по Y
        private const val X_TOLERANCE_FACTOR = 2.0F  // Фактор допуска по X
    }
}