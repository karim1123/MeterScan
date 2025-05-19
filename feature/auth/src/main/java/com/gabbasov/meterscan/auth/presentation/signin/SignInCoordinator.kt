package com.gabbasov.meterscan.auth.presentation.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.common.ui.TextFieldValue
import org.koin.androidx.compose.koinViewModel

internal class SignInCoordinator(
    private val viewModel: SignInViewModel,
    private val navController: NavHostController,
) {
    val state = viewModel.uiState

    fun onEmailChanged(email: String) {
        viewModel.execute(
            SignInAction.EmailChanged(
                TextFieldValue(
                    value = email,
                    isError = false
                )
            )
        )
    }

    fun onPasswordChanged(password: String) {
        viewModel.execute(
            SignInAction.PasswordChanged(
                TextFieldValue(
                    value = password,
                    isError = false
                ),
            ),
        )
    }

    fun onSignInPressed() {
        viewModel.execute(SignInAction.SignInPressed)
    }

    fun onNavigateToSignUp() {
        navController.navigate(viewModel.signUpApi.signUpRoute())
    }

    fun onNavigateToMainScreen() {
        navController.navigate(viewModel.mainScreenApi.mainScreenRoute()) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }
}

@Composable
internal fun rememberSignInCoordinator(
    viewModel: SignInViewModel = koinViewModel(),
    navController: NavHostController,
): SignInCoordinator {
    return remember(viewModel) {
        SignInCoordinator(
            viewModel = viewModel,
            navController = navController,
        )
    }
}