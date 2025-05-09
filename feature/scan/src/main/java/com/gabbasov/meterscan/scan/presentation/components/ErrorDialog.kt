package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabbasov.meterscan.scan.R

@Composable
fun ErrorDialog(
    onRetry: () -> Unit,
    onEnterManually: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recognition_failed)) },
        text = { Text(stringResource(R.string.could_not_recognize_meter)) },
        confirmButton = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.try_again))
            }
        },
        dismissButton = {
            TextButton(onClick = onEnterManually) {
                Text(stringResource(R.string.enter_manually))
            }
        }
    )
}