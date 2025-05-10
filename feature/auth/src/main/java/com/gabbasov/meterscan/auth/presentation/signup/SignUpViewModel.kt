package com.gabbasov.meterscan.auth.presentation.signup

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.model.auth.User
import com.gabbasov.meterscan.auth.data.repository.SignUpUseCase
import com.gabbasov.meterscan.network.Errors
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState = _uiState.asStateFlow()
    private var state: SignUpState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    fun execute(action: SignUpAction) {
        logAction(action)
        when (action) {
            is SignUpAction.EmailChanged -> {
                state = state.copy(
                    content = state.content.copy(
                        email = action.email
                    ),
                    error = null
                )
            }
            is SignUpAction.PasswordChanged -> {
                state = state.copy(
                    content = state.content.copy(
                        password = action.password
                    ),
                    error = null
                )
            }
            is SignUpAction.RepeatPasswordChanged -> {
                state = state.copy(
                    content = state.content.copy(
                        confirmPassword = action.repeatPassword
                    ),
                    error = null
                )
            }
            is SignUpAction.SignUpPressed -> {
                signUp()
            }
        }
    }

    private fun signUp() = viewModelScope.launch {
        state = state.copy(isLoading = true)
        val result = signUpUseCase.execute(
            SignUpUseCase.Params(
                email = state.content.email.value,
                password = state.content.password.value,
                repeatPassword = state.content.confirmPassword.value
            )
        )
        handleSignUpResult(result)
        state = state.copy(isLoading = false)
    }

    private fun handleSignUpResult(result: Resource<User>) {
        when (result) {
            is Resource.Error -> {
                handleError(result.exception)
            }
            is Resource.Success -> {

            }
        }
    }

    private fun handleError(error: Throwable) {
        val errorType = error.message?.let { Errors.getErrorFromName(it) } ?: return

        state = state.copy(
            content = updateContentErrorState(errorType),
            error = Errors.getErrorText(errorType)
        )
    }

    private fun updateContentErrorState(error: Errors): SignUpData {
        return when (error) {
            Errors.EMPTY_EMAIL, Errors.INVALID_EMAIL ->
                state.content.copy(email = state.content.email.copy(isError = true))

            Errors.EMPTY_PASSWORD, Errors.SHORT_PASSWORD ->
                state.content.copy(password = state.content.password.copy(isError = true))

            Errors.PASSWORDS_DO_NOT_MATCH ->
                state.content.copy(confirmPassword = state.content.confirmPassword.copy(isError = true))

            else -> state.content
        }
    }
}
