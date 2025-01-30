package com.gabbasov.meterscan.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.ui.TextFieldValue
import com.gabbasov.meterscan.ui.modifiers.hideKeyboardOnTap
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
            onEmailChanged = { email ->
                viewModel.execute(
                    SignUpAction.EmailChanged(
                        TextFieldValue(
                            value = email,
                            isError = false
                        )
                    )
                )
            },
            onPasswordChanged = { password ->
                viewModel.execute(
                    SignUpAction.PasswordChanged(
                        TextFieldValue(
                            value = password,
                            isError = false
                        ),
                    ),
                )
            },
            onRepeatPasswordChanged = { repeatPassword ->
                viewModel.execute(
                    SignUpAction.RepeatPasswordChanged(
                        TextFieldValue(
                            value = repeatPassword,
                            isError = false
                        ),
                    ),
                )
            },
            onSignUpPressed = { viewModel.execute(SignUpAction.SignUpPressed) },
        )
        LaunchedEffect(state.error) {
            /*Snackbar(
                snackbarData = SnackbarData()
            )*/

        }
    }
}

@Composable
internal fun SignUpScreen(
    state: SignUpState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onSignUpPressed: () -> Unit,
) = with(LocalContext.current) {
    Column(
        modifier = Modifier
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
        Spacer(modifier = Modifier.height(8.dp))
        PasswordTextField(
            textFieldValue = state.content.confirmPassword,
            onPasswordChanged = onRepeatPasswordChanged,
            imeAction = ImeAction.Done
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignUpPressed,
            modifier = Modifier
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
    }
}

@Composable
private fun EmailTextField(
    textFieldValue: TextFieldValue,
    onEmailChanged: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
) = with(LocalContext.current) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = textFieldValue.value,
        onValueChange = onEmailChanged,
        label = {
            textFieldValue.label?.asText()?.let {
                Text(
                    text = it,
                )
            }
        },
        singleLine = true,
        isError = textFieldValue.isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        )
    )
}

@Composable
private fun PasswordTextField(
    textFieldValue: TextFieldValue,
    onPasswordChanged: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
) = with(LocalContext.current) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = textFieldValue.value,
        onValueChange = onPasswordChanged,
        label = {
            textFieldValue.label?.asText()?.let {
                Text(it)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        singleLine = true,
        isError = textFieldValue.isError,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            },
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        ImageVector.vectorResource(id = R.drawable.visibility)
                    } else ImageVector.vectorResource(id = R.drawable.visibility_off),
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun EmailTextFieldPreview() {
    MeterScanTheme {
        EmailTextField(
            TextFieldValue("email.com", com.gabbasov.meterscan.ui.Text.RawString("Email")),
            onEmailChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordTextFieldPreview() {
    MeterScanTheme {
        PasswordTextField(
            TextFieldValue("password", com.gabbasov.meterscan.ui.Text.RawString("Password")),
            onPasswordChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordErrorTextFieldPreview() {
    MeterScanTheme {
        PasswordTextField(
            TextFieldValue(
                "password",
                com.gabbasov.meterscan.ui.Text.RawString("Password"),
                true
            ),
            onPasswordChanged = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MeterScanTheme {
        SignUpScreen(
            state =
            SignUpState(
                content =
                SignUpData(
                    email = TextFieldValue("test@example.com"),
                    password = TextFieldValue("password"),
                    confirmPassword = TextFieldValue("password"),
                ),
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onRepeatPasswordChanged = {},
            onSignUpPressed = {},
        )
    }
}
