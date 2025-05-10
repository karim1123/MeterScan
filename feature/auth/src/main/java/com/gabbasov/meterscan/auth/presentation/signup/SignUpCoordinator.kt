package com.gabbasov.meterscan.auth.presentation.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.ui.TextFieldValue
import org.koin.androidx.compose.koinViewModel

internal class SignUpCoordinator(
    private val viewModel: SignUpViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onEmailChanged(email: String) {
        viewModel.execute(
            SignUpAction.EmailChanged(
                TextFieldValue(
                    value = email,
                    isError = false
                )
            )
        )
    }

    fun onPasswordChanged(password: String) {
        viewModel.execute(
            SignUpAction.PasswordChanged(
                TextFieldValue(
                    value = password,
                    isError = false
                ),
            ),
        )
    }

    fun onRepeatPasswordChanged(repeatPassword: String) {
        viewModel.execute(
            SignUpAction.RepeatPasswordChanged(
                TextFieldValue(
                    value = repeatPassword,
                    isError = false
                ),
            ),
        )
    }

    fun onSignUpPressed() {
        viewModel.execute(SignUpAction.SignUpPressed)
    }

    fun onBackClicked() {
        navController.popBackStack()
    }
}

@Composable
internal fun rememberSignUpCoordinator(
    viewModel: SignUpViewModel = koinViewModel(),
    navController: NavHostController,
): SignUpCoordinator {
    return remember(viewModel) {
        SignUpCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}