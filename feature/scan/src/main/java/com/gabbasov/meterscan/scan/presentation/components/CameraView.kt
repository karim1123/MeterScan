package com.gabbasov.meterscan.scan.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.Camera
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.scan.domain.MeterDetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface FlashlightControl {
    fun toggleFlashlight(): Boolean
    val isFlashlightOn: Boolean
}

@SuppressLint("ViewConstructor")
class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val confidenceThreshold: Float,
    private val highConfidenceThreshold: Float,
    private val lifecycleOwner: LifecycleOwner,
    private val onDigitsDetected: (List<DigitBox>) -> Unit,
) : FrameLayout(context, attrs, defStyleAttr), MeterDetector.DetectorListener, FlashlightControl {

    companion object {
        private const val TAG = "CameraView"
        private const val MODEL_PATH = "best_float32.tflite"
        private const val LABELS_PATH = "digits_labels.txt"
    }

    private var isFlashlightEnabled = false

    private val previewView: PreviewView = PreviewView(context)
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService
    private var detector: MeterDetector? = null
    private var isProcessing = false

    init {
        addView(previewView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Создаем и инициализируем детектор
        cameraExecutor.execute {
            try {
                detector = MeterDetector(
                    context = context,
                    modelPath = MODEL_PATH,
                    labelPath = LABELS_PATH,
                    confidenceThreshold = confidenceThreshold,
                    highConfidenceThreshold = highConfidenceThreshold,
                    detectorListener = this,

                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize detector: ${e.message}")
            }
        }

        // Запускаем камеру
        startCamera()
    }

    override val isFlashlightOn: Boolean
        get() = isFlashlightEnabled

    override fun toggleFlashlight(): Boolean {
        camera?.let {
            try {
                isFlashlightEnabled = !isFlashlightEnabled
                it.cameraControl.enableTorch(isFlashlightEnabled)
                return isFlashlightEnabled
            } catch (e: Exception) {
                Log.e(TAG, "Flashlight error: ${e.message}")
            }
        }
        return false
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val rotation = previewView.display.rotation

        // Настройка селектора камеры (используем заднюю камеру)
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // Настройка превью камеры
        val preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()

        // Настройка анализатора изображений
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(previewView.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        // Устанавливаем анализатор для обработки кадров
        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            if (!isProcessing && detector != null) {
                isProcessing = true
                val bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )

                // Копируем данные из imageProxy в bitmap
                imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
                imageProxy.close()

                // Создаем матрицу поворота для правильной ориентации изображения
                val matrix = Matrix().apply {
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }

                // Применяем поворот к изображению
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                    matrix, true
                )

                // Отправляем изображение в детектор для распознавания
                detector?.detect(rotatedBitmap)
            } else {
                imageProxy.close()
            }
        }

        // Отвязываем все предыдущие use case и привязываем новые
        cameraProvider.unbindAll()
        try {
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    override fun onDetect(digitBoxes: List<DigitBox>, inferenceTime: Long) {
        onDigitsDetected(digitBoxes)
        isProcessing = false
    }

    override fun onEmptyDetect() {
        onDigitsDetected(emptyList())
        isProcessing = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraExecutor.shutdown()
        detector?.close()
    }
}
