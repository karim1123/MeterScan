package com.gabbasov.meterscan.auth.presentation.signin

import androidx.compose.runtime.Stable
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.domain.BaseAction
import com.gabbasov.meterscan.domain.BaseState
import com.gabbasov.meterscan.ui.Text
import com.gabbasov.meterscan.ui.TextFieldValue

@Stable
internal data class SignInState(
    val content: SignInData = SignInData(),
    override val isLoading: Boolean = false,
    override val error: Text? = null,
    val navigateToMainScreen: Boolean = false,
) : BaseState()

@Stable
internal data class SignInData(
    val email: TextFieldValue = TextFieldValue(
        label = Text.ResourceString(R.string.email_label),
    ),
    val password: TextFieldValue = TextFieldValue(
        label = Text.ResourceString(R.string.password_label),
    ),
)

internal sealed interface SignInAction : BaseAction {
    data class EmailChanged(val email: TextFieldValue) : SignInAction

    data class PasswordChanged(val password: TextFieldValue) : SignInAction

    data object SignInPressed : SignInAction
}
