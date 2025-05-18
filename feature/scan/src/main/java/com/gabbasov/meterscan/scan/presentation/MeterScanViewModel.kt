package com.gabbasov.meterscan.scan.presentation

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.repository.ScanSettingsRepository
import com.gabbasov.meterscan.scan.data.ScanSettingsRepositoryImpl.Companion.STABILITY_THRESHOLD
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.ui.BaseViewModel
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

class MeterScanViewModel(
    private val scanSettingsRepository: ScanSettingsRepository,
    private val metersRepository: MetersRepository,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(MeterScanState())
    val uiState = _uiState.asStateFlow()

    private var state: MeterScanState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    private var noDetectionCounter = 0  // Счетчик пустых кадров для сброса буфера

    private val recognitionBuffer = mutableListOf<List<DigitBox>>()
    private var bufferSize = 30 // Значение по умолчанию, обновится в init
    private var confidenceThreshold = 0.5f
    private var highConfidenceThreshold = 0.95f

    private val maxNoDetectionFrames: Int
        get() = (bufferSize / 3).coerceAtLeast(3)

    private val minStableFrames: Int
        get() = (bufferSize / 2).coerceAtLeast(5)

    init {
        // Загружаем настройки при инициализации
        viewModelScope.launch {
            bufferSize = scanSettingsRepository.getBufferSize()
            confidenceThreshold = scanSettingsRepository.getConfidenceThreshold()
            highConfidenceThreshold = scanSettingsRepository.getHighConfidenceThreshold()
        }
    }

    fun getСonfidenceThreshold() = confidenceThreshold

    fun getHighConfidenceThreshold() = highConfidenceThreshold

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
            is MeterScanAction.ToggleFlashlight -> toggleCamera()
            is MeterScanAction.RotateCamera -> rotateCamera()
            is MeterScanAction.TogglePause -> togglePause()
            MeterScanAction.HideMeterSelection -> hideMeterSelection()
            is MeterScanAction.LoadMeter -> loadMeter(action.meterId)
            MeterScanAction.NavigateToMetersList -> Unit
            is MeterScanAction.SelectMeter -> selectMeter(action.meter)
            MeterScanAction.ShowMeterSelection -> showMeterSelection()
        }
    }

    private fun loadMeter(meterId: String) = viewModelScope.launch {
        if (meterId.isBlank()) return@launch

        state = state.copy(isLoading = true)

        metersRepository.getMeterById(meterId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    state = state.copy(
                        selectedMeter = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = Text.RawString("Ошибка загрузки счетчика: ${result.exception.message}")
                    )
                }
            }
        }
    }

    private fun selectMeter(meter: Meter) {
        state = state.copy(
            selectedMeter = meter,
            showMeterSelection = false
        )
    }

    private fun showMeterSelection() {
        state = state.copy(showMeterSelection = true)
    }

    private fun hideMeterSelection() {
        state = state.copy(showMeterSelection = false)
    }

    private fun saveReading(reading: String) = viewModelScope.launch {
        val meter = state.selectedMeter ?: return@launch

        if (reading.isBlank()) {
            state = state.copy(
                error = Text.RawString("Показания не могут быть пустыми")
            )
            return@launch
        }

        val readingValue = reading.toDoubleOrNull()
        if (readingValue == null) {
            state = state.copy(
                error = Text.RawString("Неверный формат показаний")
            )
            return@launch
        }

        when (val result = metersRepository.addReading(meter.id, readingValue)) {
            is Resource.Success -> {
                state = state.copy(
                    showSuccessMessage = true,
                    showBottomSheet = false
                )

                delay(2000)
                state = state.copy(showSuccessMessage = false)
            }
            is Resource.Error -> {
                state = state.copy(
                    error = Text.RawString("Ошибка сохранения показаний: ${result.exception.message}")
                )
            }
        }
    }

    private fun togglePause() {
        state = state.copy(isPaused = !state.isPaused)
    }

    private fun toggleCamera() {
        state = state.copy(flashlightEnabled = !state.flashlightEnabled)
    }

    private fun rotateCamera() {
        val currentRotation = state.cameraRotation
        val nextRotation = (currentRotation + 90) % 360
        state = state.copy(cameraRotation = nextRotation)
    }

    private fun processDetectedDigits(digitBoxes: List<DigitBox>) {
        // Проверка валидности расположения цифр
        val validDigits = if (isValidDigitArrangement(digitBoxes)) {
            digitBoxes
        } else emptyList()

        // Обработка случая отсутствия обнаружений
        if (validDigits.isEmpty()) {
            noDetectionCounter++

            // Если накопилось слишком много пустых кадров, сбрасываем буфер
            if (noDetectionCounter >= maxNoDetectionFrames) {
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
        if (recognitionBuffer.size > bufferSize) {
            recognitionBuffer.removeAt(0)
        }

        // Обновляем прогресс накопления
        val progress = (recognitionBuffer.size.toFloat() / bufferSize).coerceIn(0f, 1f)

        // Определяем консенсус только если буфер накопил достаточно данных
        if (recognitionBuffer.size >= minStableFrames) {
            val (consensusReading, consensusDigits, stabilityScore) = determineConsensusReading()
            val trimmedReading = consensusReading.trimStart('0').ifEmpty { "0" }

            state = state.copy(
                recognitionProgress = progress,
                stabilityScore = stabilityScore
            )

            // Если стабильность превысила порог - показываем результат
            if (stabilityScore >= STABILITY_THRESHOLD && consensusDigits.isNotEmpty()) {
                Timber.d("Стабильное распознавание: $consensusReading (score: $stabilityScore)")
                state = state.copy(
                    detectedDigits = consensusDigits.toImmutableList(),
                    meterReading = trimmedReading,
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
            abs(it.cy - avgY) < avgHeight * 0.3
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
        // Удаляем ведущие нули, но оставляем хотя бы один ноль если строка состоит только из нулей
        val trimmedReading = reading.trimStart('0').ifEmpty { "0" }
        state = state.copy(meterReading = trimmedReading)
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