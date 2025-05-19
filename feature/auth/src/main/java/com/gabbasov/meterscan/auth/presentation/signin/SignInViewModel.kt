package com.gabbasov.meterscan.auth.presentation.signin

import androidx.lifecycle.viewModelScope
import com.gabbasov.meterscan.model.auth.User
import com.gabbasov.meterscan.auth.data.repository.SignInUseCase
import com.gabbasov.meterscan.features.MainScreenFeatureApi
import com.gabbasov.meterscan.features.SignUpFeatureApi
import com.gabbasov.meterscan.common.network.Errors
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.common.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    val signUpApi: SignUpFeatureApi,
    val mainScreenApi: MainScreenFeatureApi,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(SignInState())
    val uiState = _uiState.asStateFlow()
    private var state: SignInState
        get() = _uiState.value
        set(value) {
            _uiState.update { value }
        }

    fun execute(action: SignInAction) {
        logAction(action)
        when (action) {
            is SignInAction.EmailChanged -> {
                state = state.copy(
                    content = state.content.copy(
                        email = action.email
                    ),
                    error = null
                )
            }

            is SignInAction.PasswordChanged -> {
                state = state.copy(
                    content = state.content.copy(
                        password = action.password
                    ),
                    error = null
                )
            }

            is SignInAction.SignInPressed -> {
                signIn()
            }
        }
    }

    private fun signIn() = viewModelScope.launch {
        state = state.copy(isLoading = true)
        val result = signInUseCase.execute(
            SignInUseCase.Params(
                email = state.content.email.value,
                password = state.content.password.value,
            )
        )
        handleSignInResult(result)
        state = state.copy(isLoading = false)
    }

    private fun handleSignInResult(result: Resource<User>) {
        when (result) {
            is Resource.Error -> {
                handleError(result.exception)
            }

            is Resource.Success -> {
                state = state.copy(
                    navigateToMainScreen = true,
                    error = null
                )
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

    private fun updateContentErrorState(error: Errors): SignInData {
        return when (error) {
            Errors.EMPTY_EMAIL, Errors.INVALID_EMAIL ->
                state.content.copy(email = state.content.email.copy(isError = true))

            Errors.EMPTY_PASSWORD, Errors.SHORT_PASSWORD ->
                state.content.copy(password = state.content.password.copy(isError = true))

            else -> state.content
        }
    }
}
