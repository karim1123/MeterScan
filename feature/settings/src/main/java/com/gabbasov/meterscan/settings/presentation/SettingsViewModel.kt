package com.gabbasov.meterscan.settings.presentation

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.features.SignInFeatureApi
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.repository.AuthRepository
import com.gabbasov.meterscan.repository.ScanSettingsRepository
import com.gabbasov.meterscan.repository.SettingsRepository
import com.gabbasov.meterscan.common.ui.BaseViewModel
import com.gabbasov.meterscan.common.ui.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val scanSettingsRepository: ScanSettingsRepository,
    private val authRepository: AuthRepository,
    val signInApi: SignInFeatureApi,
    ) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    private var state: SettingsState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    init {
        loadUserData()
        loadSettings()
        loadScanSettings()
    }

    fun execute(action: SettingsAction) {
        logAction(action)
        when (action) {
            is SettingsAction.SetCameraMode -> setCameraMode(action.enabled)
            is SettingsAction.SetNavigatorType -> setNavigatorType(action.type)
            is SettingsAction.SignOut -> signOut()
            is SettingsAction.SetBufferSize -> setBufferSize(action.size)
            is SettingsAction.SetConfidenceThreshold -> setConfidenceThreshold(action.threshold)
            is SettingsAction.SetHighConfidenceThreshold -> setHighConfidenceThreshold(action.threshold)

        }
    }

    private fun loadUserData() = viewModelScope.launch {
        authRepository.getCurrentUser().collect { result ->
            when (result) {
                is Resource.Success -> {
                    state = state.copy(userEmail = result.data?.email)
                }

                is Resource.Error -> {
                    // Обработка ошибки при загрузке пользователя
                }
            }
        }
    }

    private fun loadSettings() = viewModelScope.launch {
        val cameraMode = settingsRepository.getCameraMode()
        val navigatorType = settingsRepository.getNavigatorType()
        val appVersion = settingsRepository.getAppVersion()

        state = state.copy(
            useCameraMode = cameraMode,
            navigatorType = navigatorType,
            appVersion = appVersion
        )
    }

    private fun setCameraMode(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.saveCameraMode(enabled)
        state = state.copy(useCameraMode = enabled)
    }

    private fun setNavigatorType(type: NavigatorType) = viewModelScope.launch {
        settingsRepository.saveNavigatorType(type)
        state = state.copy(navigatorType = type)
    }

    /**
     * Выход из аккаунта пользователя
     * Очищает данные пользователя и вызывает signOut у authRepository
     * После выхода SettingsCoordinator перенаправит на экран авторизации
     */
    private fun signOut() = viewModelScope.launch {
        try {
            state = state.copy(isLoading = true)
            when (val result = authRepository.signOut()) {
                is Resource.Success -> {
                    state = state.copy(
                        userEmail = null,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    handleError(result.exception.message)
                }
            }
        } catch (e: Exception) {
            handleError("Ошибка при выходе из аккаунта: ${e.message}")
        } finally {
            state = state.copy(isLoading = false)
        }
    }

    private fun loadScanSettings() = viewModelScope.launch {
        val bufferSize = scanSettingsRepository.getBufferSize()
        val confidenceThreshold = scanSettingsRepository.getConfidenceThreshold()
        val highConfidenceThreshold = scanSettingsRepository.getHighConfidenceThreshold()

        state = state.copy(
            bufferSize = bufferSize,
            confidenceThreshold = confidenceThreshold,
            highConfidenceThreshold = highConfidenceThreshold
        )
    }

    private fun setBufferSize(size: Int) = viewModelScope.launch {
        scanSettingsRepository.setBufferSize(size)
        state = state.copy(bufferSize = size)
    }

    private fun setConfidenceThreshold(threshold: Float) = viewModelScope.launch {
        scanSettingsRepository.setConfidenceThreshold(threshold)
        state = state.copy(confidenceThreshold = threshold)
    }

    private fun setHighConfidenceThreshold(threshold: Float) = viewModelScope.launch {
        scanSettingsRepository.setHighConfidenceThreshold(threshold)
        state = state.copy(highConfidenceThreshold = threshold)
    }

    private fun handleError(errorMessage: String?) {
        state = state.copy(
            error = errorMessage?.let { Text.RawString(it)},
            isLoading = false
        )
    }
}