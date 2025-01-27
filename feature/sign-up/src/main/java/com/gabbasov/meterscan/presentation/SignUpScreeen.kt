package com.gabbasov.meterscan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SignUpScreenRoute(
    navController: NavHostController,
    viewModel: SignUpViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MeterScanTheme {
        SignUpScreen(
            state = state,
            onEmailChanged = { email -> viewModel.execute(SignUpAction.EmailChanged(email)) },
            onPasswordChanged = { password -> viewModel.execute(SignUpAction.PasswordChanged(password)) },
            onRepeatPasswordChanged = { repeatPassword -> viewModel.execute(SignUpAction.RepeatPasswordChanged(repeatPassword)) },
            onSignUpPressed = { viewModel.execute(SignUpAction.SignUpPressed) },
        )
    }
}

@Composable
internal fun SignUpScreen(
    state: SignUpState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onSignUpPressed: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = state.content.email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = state.content.password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = state.content.confirmPassword,
            onValueChange = onRepeatPasswordChanged,
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignUpPressed,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text = "Sign Up")
            }
        }
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SignUpScreenPreview() {
    MeterScanTheme {
        SignUpScreen(
            state =
                SignUpState(
                    isLoading = false,
                    content = SignUpData(email = "test@example.com", password = "password", confirmPassword = "password"),
                    error = null,
                ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onRepeatPasswordChanged = {},
            onSignUpPressed = {},
        )
    }
}
