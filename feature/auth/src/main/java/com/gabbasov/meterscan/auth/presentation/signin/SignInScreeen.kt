package com.gabbasov.meterscan.auth.presentation.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.auth.presentation.components.ActionButton
import com.gabbasov.meterscan.auth.presentation.components.EmailTextField
import com.gabbasov.meterscan.auth.presentation.components.NavigateToSignUpText
import com.gabbasov.meterscan.auth.presentation.components.PasswordTextField
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.ui.TextFieldValue
import com.gabbasov.meterscan.ui.modifiers.hideKeyboardOnTap
import kotlinx.coroutines.launch

@Composable
internal fun SignInScreenRoute(
    coordinator: SignInCoordinator,
) = with(LocalContext.current) {
    val state by coordinator.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorText.asText())
            }
        }
    }

    MeterScanTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            SignUpScreen(
                modifier = Modifier.padding(paddingValues),
                state = state,
                onEmailChanged = coordinator::onEmailChanged,
                onPasswordChanged = coordinator::onPasswordChanged,
                onSignInPressed = coordinator::onSignInPressed,
                onNavigateToSignUp = coordinator::onNavigateToSignUp
            )
        }
    }
}

@Composable
internal fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignInState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInPressed: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .hideKeyboardOnTap()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailTextField(
            textFieldValue = state.content.email,
            onEmailChanged = onEmailChanged,
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(8.dp))
        PasswordTextField(
            textFieldValue = state.content.password,
            onPasswordChanged = onPasswordChanged,
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(16.dp))
        ActionButton(
            text = stringResource(R.string.sign_in),
            onClick = onSignInPressed,
            isLoading = state.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigateToSignUpText(
            onNavigateToSignUp = onNavigateToSignUp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MeterScanTheme {
        SignUpScreen(
            state =
            SignInState(
                content =
                SignInData(
                    email = TextFieldValue("test@example.com"),
                    password = TextFieldValue("password"),
                ),
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onSignInPressed = {},
            onNavigateToSignUp = {}
        )
    }
}
