package com.gabbasov.meterscan.scan.domain

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
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

    init {
        Log.d("test312", "Initializing detector with model: $modelPath, labels: $labelPath")

        val compatList = CompatibilityList()

        val options = Interpreter.Options().apply {
            val delegateOptions = compatList.bestOptionsForThisDevice
            this.addDelegate(GpuDelegate(delegateOptions))
            this.setUseNNAPI(true) // Включение NNAPI
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)
        Log.d("test312", "Interpreter initialized")

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
            Log.d("test312", "Input shape: ${inputShape.contentToString()}, tensor size: $tensorWidth x $tensorHeight")
        }

        if (outputShape != null) {
            numChannel = outputShape[1]
            numElements = outputShape[2]
            Log.d("test312", "Output shape: ${outputShape.contentToString()}, numChannel: $numChannel, numElements: $numElements")
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
            Log.d("test312", "Loaded ${labels.size} labels: $labels")
        } catch (e: IOException) {
            Log.e("test312", "Failed to load labels: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun close() {
        interpreter.close()
    }

    fun detect(frame: Bitmap) {
        Log.d("test312", "Detecting on bitmap: ${frame.width}x${frame.height}")

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
        Log.d("test312", "Tensor image created and processed")

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter.run(imageBuffer, output.buffer)
        Log.d("test312", "Inference completed, output size: ${output.floatArray.size}")

        val detectedDigits = processOutput(output.floatArray)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        Log.d("test312", "Detection completed in $inferenceTime ms, found ${detectedDigits.size} digits")

        if (detectedDigits.isEmpty()) {
            detectorListener.onEmptyDetect()
            return
        }

        detectorListener.onDetect(detectedDigits, inferenceTime)
    }

    /**
     * Обрабатывает результат распознавания, сортирует цифры по X-координате и фильтрует шум
     */
    private fun processOutput(array: FloatArray): List<DigitBox> {
        Log.d("test312", "Processing output array of size ${array.size}, labels count: ${labels.size}")

        if (labels.isEmpty()) {
            Log.e("test312", "Labels list is empty. Check if labels file exists and readable.")
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

                Log.d("test312", "Detected digit: $digitValue at ($cx,$cy) with confidence $maxConf")

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

        Log.d("test312", "Initial digit boxes count: ${digitBoxes.size}")

        if (digitBoxes.isEmpty()) return emptyList()

        // Применяем NMS для устранения дубликатов
        val filteredBoxes = applyNMS(digitBoxes)
        Log.d("test312", "After NMS: ${filteredBoxes.size} boxes")

        // Фильтруем шум - убираем цифры, которые находятся далеко от остальных
        val result = filterOutliers(filteredBoxes)
        Log.d("test312", "After outlier filtering: ${result.size} boxes, sorted: ${result.sortedBy { it.cx }.map { it.digit }}")

        return result
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
            Math.abs(it.cy - avgY) < avgHeight * Y_TOLERANCE_FACTOR
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
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
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
        private const val CONFIDENCE_THRESHOLD = 0.6F //todo добавить настройку
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.99F
        private const val IOU_THRESHOLD = 0.5F
        private const val Y_TOLERANCE_FACTOR = 0.5F  // Фактор допуска по Y
        private const val X_TOLERANCE_FACTOR = 2.0F  // Фактор допуска по X
    }
}