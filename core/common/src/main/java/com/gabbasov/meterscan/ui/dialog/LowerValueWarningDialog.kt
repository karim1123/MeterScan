package com.gabbasov.meterscan.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabbasov.meterscan.R

@Composable
fun LowerValueWarningDialog(
    reading: String,
    lastReading: Double,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lower_value_tile)) },
        text = {
            Text(stringResource(R.string.lower_value_body, reading, lastReading))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.lower_value_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.lower_value_decline))
            }
        }
    )
}

@Preview
@Composable
fun LowerValueWarningDialogPreview() {
    LowerValueWarningDialog(
        reading = "100",
        lastReading = 200.0,
        onConfirm = {},
        onEdit = {},
        onDismiss = {}
    )
}
