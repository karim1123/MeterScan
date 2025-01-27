package com.gabbasov.meterscan.presentation

internal data class SignUpState(
    val isLoading: Boolean = false,
    val content: SignUpData = SignUpData(),
    val error: String? = null,
)

internal data class SignUpData(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
)

internal sealed interface SignUpAction {
    data class EmailChanged(val email: String) : SignUpAction

    data class PasswordChanged(val password: String) : SignUpAction

    data class RepeatPasswordChanged(val repeatPassword: String) : SignUpAction

    data object SignUpPressed : SignUpAction
    /*
    object ErrorObserved : SignUpAction
    object NavigationObserved : SignUpAction
     */
}
