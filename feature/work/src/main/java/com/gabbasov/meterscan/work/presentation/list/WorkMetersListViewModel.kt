package com.gabbasov.meterscan.work.presentation.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.ui.BaseViewModel
import com.gabbasov.meterscan.ui.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WorkMetersListViewModel(
    private val metersRepository: MetersRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(WorkMetersListState())
    val uiState = _uiState.asStateFlow()

    private var state: WorkMetersListState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    init {
        execute(WorkMetersListAction.LoadMeters)
    }

    fun execute(action: WorkMetersListAction) {
        logAction(action)
        when (action) {
            is WorkMetersListAction.LoadMeters -> loadMeters()
            is WorkMetersListAction.MeterSelected -> Unit // Обработка в координаторе
            is WorkMetersListAction.AddNewMeter -> Unit // Обработка в координаторе
            is WorkMetersListAction.SearchMeters -> searchMeters(action.query)
        }
    }

    private fun loadMeters() = viewModelScope.launch {
        state = state.copy(isLoading = true)

        metersRepository.getAllMeters().collect { result ->
            state = when (result) {
                is Resource.Success -> {
                    state.copy(
                        isLoading = false,
                        meters = result.data.toImmutableList(),
                        filteredMeters = filterMeters(result.data, state.searchQuery).toImmutableList(),
                        error = null
                    )
                }

                is Resource.Error -> {
                    Log.d("test312", "result.exception: ${result.exception}")
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
}