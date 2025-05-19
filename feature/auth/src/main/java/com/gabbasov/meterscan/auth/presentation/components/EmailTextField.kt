package com.gabbasov.meterscan.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.common.ui.TextFieldValue

@Composable
internal fun EmailTextField(
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

@Preview(showBackground = true)
@Composable
private fun EmailTextFieldPreview() {
    MeterScanTheme {
        EmailTextField(
            TextFieldValue("email.com", com.gabbasov.meterscan.common.ui.Text.RawString("Email")),
            onEmailChanged = {},
        )
    }
}