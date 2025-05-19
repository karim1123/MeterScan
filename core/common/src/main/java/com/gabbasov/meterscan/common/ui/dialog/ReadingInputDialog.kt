package com.gabbasov.meterscan.common.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.gabbasov.meterscan.common.R

@Composable
fun ReadingInputDialog(
    lastReading: Double?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var reading by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_reading_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = reading,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            reading = it
                            isError = false
                        }
                    },
                    label = { Text(stringResource(R.string.add_reading_label)) },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (lastReading != null) {
                    Text(
                        stringResource(R.string.add_reading_last_reading, lastReading),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val readingValue = reading.toDoubleOrNull()
                    if (readingValue != null) {
                        onSave(reading)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.add_reading_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_reading_decline))
            }
        }
    )
}

@Preview
@Composable
fun ReadingInputDialogPreview() {
    ReadingInputDialog(
        lastReading = 100.0,
        onDismiss = {},
        onSave = {}
    )
}

