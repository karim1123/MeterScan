package com.gabbasov.meterscan.auth.presentation

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.ui.Text
import com.gabbasov.meterscan.ui.TextFieldValue

@Stable
internal data class SignUpState(
    val content: SignUpData = SignUpData(),
    override val isLoading: Boolean = false,
    override val error: Text? = null,
) : BaseState()

@Stable
internal data class SignUpData(
    val email: TextFieldValue =
        TextFieldValue(
            label = Text.ResourceString(R.string.email_label),
        ),
    val password: TextFieldValue =
        TextFieldValue(
            label = Text.ResourceString(R.string.password_label),
        ),
    val confirmPassword: TextFieldValue =
        TextFieldValue(
            label = Text.ResourceString(R.string.confirm_password_label),
        ),
)

internal sealed interface SignUpAction : BaseAction {
    data class EmailChanged(val email: TextFieldValue) : SignUpAction

    data class PasswordChanged(val password: TextFieldValue) : SignUpAction

    data class RepeatPasswordChanged(val repeatPassword: TextFieldValue) : SignUpAction

    data object SignUpPressed : SignUpAction

    data object NavToLogInPressed : SignUpAction
}
