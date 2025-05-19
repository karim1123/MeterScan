package com.gabbasov.meterscan.work.presentation.list

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.repository.SettingsRepository
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.common.ui.BaseViewModel
import com.gabbasov.meterscan.common.ui.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WorkViewModel(
    private val metersRepository: MetersRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(WorkState())
    val uiState = _uiState.asStateFlow()

    private var state: WorkState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    init {
        execute(WorkAction.LoadMeters)
        execute(WorkAction.LoadNavigatorType)
    }

    fun execute(action: WorkAction) {
        logAction(action)
        when (action) {
            is WorkAction.LoadMeters -> loadMeters()
            is WorkAction.MeterSelected -> Unit // Обработка в координаторе
            is WorkAction.TakeReading -> showReadingDialog(action.meterId)
            is WorkAction.SearchMeters -> searchMeters(action.query)
            is WorkAction.NavigationHandled -> state = state.copy(navigateToScan = null)
            is WorkAction.UpdateMeterState -> updateMeterState(action.meterId, action.newState)
            is WorkAction.SaveReading -> checkAndSaveReading(action.reading)
            is WorkAction.ConfirmLowerValue -> forceAddReading()
            is WorkAction.DismissReadingDialog -> state = state.copy(showReadingDialog = false)
            is WorkAction.DismissLowerValueWarning -> state = state.copy(showLowerValueWarning = false)
            is WorkAction.ShowReadingDialog -> showReadingDialog(action.meterId)
            is WorkAction.SetUserLocation -> state = state.copy(userLocation = action.location)
            is WorkAction.LoadNavigatorType -> loadNavigatorType()
            is WorkAction.BuildRoute -> Unit // Обработка через MapBottomSheet и координатор
            is WorkAction.SetSelectedTabIndex -> Unit
        }
    }

    private fun loadNavigatorType() = viewModelScope.launch {
        try {
            val navigatorType = settingsRepository.getNavigatorType()
            state = state.copy(navigatorType = navigatorType)
        } catch (e: Exception) {
            state = state.copy(
                error = Text.RawString("Ошибка загрузки настроек навигатора: ${e.message}")
            )
        }
    }

    private fun showReadingDialog(meterId: String) {
        viewModelScope.launch {
            // Загружаем актуальные данные счетчика перед показом диалога
            val meterResource = metersRepository.getMeterById(meterId).first()
            if (meterResource is Resource.Success) {
                state = state.copy(
                    showReadingDialog = true,
                    selectedMeterId = meterId
                )
            } else {
                state = state.copy(
                    error = Text.RawString("Ошибка загрузки данных счетчика")
                )
            }
        }
    }

    private fun checkAndSaveReading(reading: String) {
        val readingValue = reading.toDoubleOrNull() ?: return
        val meterId = state.selectedMeterId ?: return

        viewModelScope.launch {
            // Получаем актуальные данные счетчика
            val meterResource = metersRepository.getMeterById(meterId).first()

            if (meterResource is Resource.Error) {
                state = state.copy(
                    error = Text.RawString("Ошибка получения данных счетчика")
                )
                return@launch
            }

            val meter = (meterResource as Resource.Success).data

            // Проверяем последнее показание
            val lastReading = meter.readings.maxByOrNull { it.date }?.value ?: 0.0

            if (readingValue < lastReading) {
                state = state.copy(
                    showLowerValueWarning = true,
                    newReading = reading,
                    showReadingDialog = false
                )
            } else {
                addReadingToMeter(readingValue)
            }
        }
    }

    private fun forceAddReading() {
        val readingValue = state.newReading.toDoubleOrNull() ?: return
        addReadingToMeter(readingValue)
    }

    private fun addReadingToMeter(reading: Double) = viewModelScope.launch {
        val meterId = state.selectedMeterId ?: return@launch

        state = state.copy(isLoading = true, showReadingDialog = false, showLowerValueWarning = false)

        try {
            // Добавляем показания
            val result = metersRepository.addReading(meterId, reading)

            when (result) {
                is Resource.Success -> {
                    state = state.copy(
                        isLoading = false,
                        selectedMeterId = null
                    )

                    // Перезагружаем все счетчики
                    loadMetersWithReadings()
                }
                is Resource.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = Text.RawString("Ошибка сохранения показаний: ${result.exception.message}")
                    )
                }
            }
        } catch (e: Exception) {
            state = state.copy(
                isLoading = false,
                error = Text.RawString("Ошибка: ${e.message}")
            )
        }
    }

    // Метод для загрузки счетчиков с показаниями
    private fun loadMetersWithReadings() = viewModelScope.launch {
        state = state.copy(isLoading = true)

        try {
            val allMeters = mutableListOf<Meter>()

            // Получаем список всех счетчиков
            val metersResult = metersRepository.getAllMeters().first()
            if (metersResult is Resource.Error) {
                throw metersResult.exception
            }

            val basicMeters = (metersResult as Resource.Success).data

            // Для каждого счетчика загружаем полные данные с показаниями
            for (basicMeter in basicMeters) {
                val detailedMeterResult = metersRepository.getMeterById(basicMeter.id).first()
                if (detailedMeterResult is Resource.Success) {
                    val detailedMeter = detailedMeterResult.data
                    allMeters.add(detailedMeter)
                } else {
                    // Если не удалось получить детальные данные, используем базовые
                    allMeters.add(basicMeter)
                }
            }

            state = state.copy(
                isLoading = false,
                meters = allMeters.toImmutableList(),
                filteredMeters = filterMeters(allMeters, state.searchQuery).toImmutableList(),
                error = null
            )
        } catch (e: Exception) {
            state = state.copy(
                isLoading = false,
                error = Text.RawString("Ошибка загрузки счетчиков: ${e.message}")
            )
        }
    }

    private fun loadMeters() = viewModelScope.launch {
        state = state.copy(isLoading = true)

        metersRepository.getAllMeters().collect { result ->
            state = when (result) {
                is Resource.Success -> {
                    // Заменяем стандартную загрузку на загрузку с показаниями
                    loadMetersWithReadings()

                    // Временное состояние до завершения loadMetersWithReadings
                    state.copy(
                        isLoading = true,
                        error = null
                    )
                }
                is Resource.Error -> {
                    state.copy(
                        isLoading = false,
                        error = Text.RawString(
                            result.exception.message ?: "Ошибка загрузки счетчиков"
                        )
                    )
                }
            }
        }
    }

    private fun searchMeters(query: String) {
        state = state.copy(
            searchQuery = query,
            filteredMeters = filterMeters(state.meters, query).toImmutableList()
        )
    }

    private fun filterMeters(meters: List<Meter>, query: String): List<Meter> {
        if (query.isBlank()) return meters

        val lowercaseQuery = query.lowercase()
        return meters.filter { meter ->
            meter.number.lowercase().contains(lowercaseQuery) ||
                    meter.address.getFullAddress().lowercase().contains(lowercaseQuery) ||
                    meter.owner.lowercase().contains(lowercaseQuery) ||
                    meter.type.name.lowercase().contains(lowercaseQuery)
        }
    }

    private fun updateMeterState(meterId: String, newState: MeterState) = viewModelScope.launch {
        // Получаем актуальные данные счетчика
        val meterResource = metersRepository.getMeterById(meterId).first()

        if (meterResource is Resource.Error) {
            state = state.copy(
                error = Text.RawString("Ошибка получения данных счетчика")
            )
            return@launch
        }

        val meter = (meterResource as Resource.Success).data
        val updatedMeter = meter.copy(state = newState)

        when (val result = metersRepository.updateMeter(updatedMeter)) {
            is Resource.Success -> {
                if (newState == MeterState.SAVED_LOCALLY) {
                    simulateServerSubmission(meterId)
                } else {
                    loadMetersWithReadings()
                }
            }
            is Resource.Error -> {
                state = state.copy(
                    error = Text.RawString(
                        result.exception.message ?: "Ошибка обновления состояния счетчика"
                    )
                )
            }
        }
    }

    private fun simulateServerSubmission(meterId: String) = viewModelScope.launch {
        // Получаем актуальные данные счетчика
        val meterResource = metersRepository.getMeterById(meterId).first()

        if (meterResource is Resource.Error) {
            state = state.copy(
                error = Text.RawString("Ошибка получения данных счетчика")
            )
            return@launch
        }

        val meter = (meterResource as Resource.Success).data
        val updatedMeter = meter.copy(state = MeterState.SUBMITTED_TO_SERVER)

        when (val result = metersRepository.updateMeter(updatedMeter)) {
            is Resource.Success -> loadMetersWithReadings()
            is Resource.Error -> {
                state = state.copy(
                    error = Text.RawString(
                        result.exception.message ?: "Ошибка отправки на сервер"
                    )
                )
            }
        }
    }
}