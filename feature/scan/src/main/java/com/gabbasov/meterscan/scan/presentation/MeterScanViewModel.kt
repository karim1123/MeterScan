package com.gabbasov.meterscan.scan.presentation

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private val recognitionBuffer = mutableListOf<List<DigitBox>>()
private const val BUFFER_SIZE = 30     // Размер буфера накопления
private const val MIN_STABLE_FRAMES = 20 // Минимальное количество стабильных кадров
private const val STABILITY_THRESHOLD = 0.7f // Порог стабильности (70%)
private const val MAX_NO_DETECTION_FRAMES = 20 // Макс. количество кадров без обнаружения

class MeterScanViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(MeterScanState())
    val uiState = _uiState.asStateFlow()

    private var state: MeterScanState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }


    // Счетчик пустых кадров для сброса буфера
    private var noDetectionCounter = 0

    fun execute(action: MeterScanAction) {
        logAction(action)
        when (action) {
            is MeterScanAction.DetectDigits -> processDetectedDigits(action.digitBoxes)
            is MeterScanAction.UpdateReading -> updateMeterReading(action.reading)
            is MeterScanAction.SaveReading -> saveReading(action.reading)
            is MeterScanAction.RetryScanning -> retryScanning()
            is MeterScanAction.EnterManually -> showEditDialog()
            is MeterScanAction.DismissBottomSheet -> dismissBottomSheet()
            is MeterScanAction.DismissErrorDialog -> dismissErrorDialog()
        }
    }

    private fun processDetectedDigits(digitBoxes: List<DigitBox>) {
        // Проверка валидности расположения цифр
        val validDigits = if (isValidDigitArrangement(digitBoxes)) {
            digitBoxes
        } else {
            emptyList()
        }

        // Обработка случая отсутствия обнаружений
        if (validDigits.isEmpty()) {
            noDetectionCounter++

            // Если накопилось слишком много пустых кадров, сбрасываем буфер
            if (noDetectionCounter >= MAX_NO_DETECTION_FRAMES) {
                recognitionBuffer.clear()
                noDetectionCounter = 0
                state = state.copy(
                    recognitionProgress = 0f,
                    stabilityScore = 0f
                )
            }
            return
        }

        // Сбрасываем счетчик пустых кадров при успешном обнаружении
        noDetectionCounter = 0

        // Добавляем в буфер новое обнаружение
        recognitionBuffer.add(validDigits)
        if (recognitionBuffer.size > BUFFER_SIZE) {
            recognitionBuffer.removeAt(0)
        }

        // Обновляем прогресс накопления
        val progress = (recognitionBuffer.size.toFloat() / BUFFER_SIZE).coerceIn(0f, 1f)

        // Определяем консенсус только если буфер накопил достаточно данных
        if (recognitionBuffer.size >= MIN_STABLE_FRAMES) {
            val (consensusReading, consensusDigits, stabilityScore) = determineConsensusReading()

            state = state.copy(
                recognitionProgress = progress,
                stabilityScore = stabilityScore
            )

            // Если стабильность превысила порог - показываем результат
            if (stabilityScore >= STABILITY_THRESHOLD && consensusDigits.isNotEmpty()) {
                Timber.d("Стабильное распознавание: $consensusReading (score: $stabilityScore)")
                state = state.copy(
                    detectedDigits = consensusDigits,
                    meterReading = consensusReading,
                    showBottomSheet = true,
                    isScanning = false,
                    recognitionProgress = 1.0f
                )

                // Очищаем буфер после успешного распознавания
                recognitionBuffer.clear()
            }
        } else {
            // Если буфер еще накапливается, просто обновляем прогресс
            state = state.copy(
                recognitionProgress = progress,
                stabilityScore = 0f
            )
        }
    }

    private fun isValidDigitArrangement(digitBoxes: List<DigitBox>): Boolean {
        if (digitBoxes.size < 2) return true

        // Проверяем, что цифры расположены примерно на одной линии
        val avgY = digitBoxes.map { it.cy }.average()
        val avgHeight = digitBoxes.map { it.h }.average()

        // Все цифры должны находиться в пределах ±30% от средней высоты
        val areDigitsAligned = digitBoxes.all {
            Math.abs(it.cy - avgY) < avgHeight * 0.3
        }

        // Проверяем, что цифры идут последовательно слева направо
        val sortedDigits = digitBoxes.sortedBy { it.cx }
        val avgWidth = sortedDigits.map { it.w }.average()
        val isSequential = (0 until sortedDigits.size - 1).all { i ->
            val currentX = sortedDigits[i].cx
            val nextX = sortedDigits[i + 1].cx
            val minDistance = avgWidth * 0.5 // Минимальное расстояние между цифрами
            val maxDistance = avgWidth * 3.0 // Максимальное расстояние

            (nextX - currentX) in minDistance..maxDistance
        }

        return areDigitsAligned && isSequential
    }

    private fun determineConsensusReading(): Triple<String, List<DigitBox>, Float> {
        // Подсчитываем частоту появления каждой последовательности цифр
        val readingFrequency = mutableMapOf<String, Int>()
        val readingDigitBoxes = mutableMapOf<String, List<DigitBox>>()

        for (detections in recognitionBuffer) {
            if (detections.isEmpty()) continue

            // Сортируем и формируем строку показаний
            val sortedDigits = detections.sortedBy { it.cx }
            val reading = sortedDigits.joinToString("") { it.digit }

            readingFrequency[reading] = (readingFrequency[reading] ?: 0) + 1
            readingDigitBoxes[reading] = sortedDigits
        }

        // Находим самое часто встречающееся показание
        val mostFrequentEntry = readingFrequency.entries.maxByOrNull { it.value }

        if (mostFrequentEntry != null) {
            val (reading, frequency) = mostFrequentEntry
            val stabilityScore = frequency.toFloat() / recognitionBuffer.size
            return Triple(reading, readingDigitBoxes[reading] ?: emptyList(), stabilityScore)
        }

        return Triple("", emptyList(), 0f)
    }

    private fun updateMeterReading(reading: String) {
        state = state.copy(meterReading = reading)
    }

    private fun saveReading(reading: String) = viewModelScope.launch {
        state = state.copy(isLoading = true)

        // Здесь должен быть вызов репозитория для сохранения показаний
        // Например: val result = meterRepository.saveReading(reading)

        // Имитация сохранения
        val result = Resource.Success(Unit)

        when (result) {
            is Resource.Success -> {
                state = state.copy(
                    isLoading = false,
                    showBottomSheet = false,
                    showSuccessMessage = true
                )
            }
            /*is Resource.Error -> {
                state = state.copy(
                    isLoading = false,
                    error = com.gabbasov.meterscan.ui.Text.RawString(
                        result.exception.message ?: "Ошибка сохранения показаний"
                    )
                )
            }*/
        }
    }

    private fun retryScanning() {
        recognitionBuffer.clear() // Сбрасываем буфер при повторном сканировании
        noDetectionCounter = 0

        state = state.copy(
            isScanning = true,
            showErrorDialog = false,
            recognitionProgress = 0f,
            stabilityScore = 0f
        )
    }

    private fun showEditDialog() {
        state = state.copy(
            showBottomSheet = true,
            showErrorDialog = false
        )
    }

    private fun dismissBottomSheet() {
        state = state.copy(showBottomSheet = false)
    }

    private fun dismissErrorDialog() {
        state = state.copy(showErrorDialog = false)
    }
}