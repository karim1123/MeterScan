package com.gabbasov.meterscan.auth.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.auth.presentation.components.ActionButton
import com.gabbasov.meterscan.auth.presentation.components.EmailTextField
import com.gabbasov.meterscan.auth.presentation.components.PasswordTextField
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.common.ui.TextFieldValue
import com.gabbasov.meterscan.common.ui.modifiers.hideKeyboardOnTap
import kotlinx.coroutines.launch

@Composable
internal fun SignUpScreenRoute(
    coordinator: SignUpCoordinator,
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
            topBar = {
                SignUpTopBar(onBackClicked = coordinator::onBackClicked)
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            SignUpScreen(
                modifier = Modifier.padding(paddingValues),
                state = state,
                onEmailChanged = coordinator::onEmailChanged,
                onPasswordChanged = coordinator::onPasswordChanged,
                onRepeatPasswordChanged = coordinator::onRepeatPasswordChanged,
                onSignUpPressed = coordinator::onSignUpPressed,
            )
        }
    }
}

@Composable
internal fun SignUpScreen(
    modifier: Modifier = Modifier,
    state: SignUpState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onSignUpPressed: () -> Unit,
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
        Spacer(modifier = Modifier.height(8.dp))
        PasswordTextField(
            textFieldValue = state.content.confirmPassword,
            onPasswordChanged = onRepeatPasswordChanged,
            imeAction = ImeAction.Done
        )
        Spacer(modifier = Modifier.height(16.dp))
        ActionButton(
            text = stringResource(R.string.sign_up),
            onClick = onSignUpPressed,
            isLoading = state.isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpTopBar(
    onBackClicked: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sign_up),
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SignUpTopBarPreview() {
    MeterScanTheme {
        SignUpTopBar(onBackClicked = {})
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
