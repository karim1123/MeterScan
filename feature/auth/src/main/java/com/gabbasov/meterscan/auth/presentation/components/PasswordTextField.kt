package com.gabbasov.meterscan.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.gabbasov.meterscan.auth.R
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.common.ui.TextFieldValue

@Composable
internal fun PasswordTextField(
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
private fun PasswordTextFieldPreview() {
    MeterScanTheme {
        PasswordTextField(
            TextFieldValue("password", com.gabbasov.meterscan.common.ui.Text.RawString("Password")),
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
                com.gabbasov.meterscan.common.ui.Text.RawString("Password"),
                true
            ),
            onPasswordChanged = {},
        )
    }
}
