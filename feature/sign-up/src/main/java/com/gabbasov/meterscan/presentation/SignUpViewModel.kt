package com.gabbasov.meterscan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.data.repository.SignUpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal class SignUpViewModel(
    private val signUpRepository: SignUpRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState = _uiState.asStateFlow()
    private var state: SignUpState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    fun execute(action: SignUpAction) {
        when (action) {
            is SignUpAction.EmailChanged -> {
                state =
                    state.copy(
                        content = state.content.copy(email = action.email),
                    )
            }
            is SignUpAction.PasswordChanged -> {
                state =
                    state.copy(
                        content = state.content.copy(password = action.password),
                    )
            }
            is SignUpAction.RepeatPasswordChanged -> {
                state =
                    state.copy(
                        content = state.content.copy(confirmPassword = action.repeatPassword),
                    )
            }
            SignUpAction.SignUpPressed -> {
                signUp()
            }
        }
    }

    private fun signUp() =
        viewModelScope.launch {
            val result = signUpRepository.signUp(state.content.email, state.content.password)
            Timber.d("test321 result: $result")
        }
}
