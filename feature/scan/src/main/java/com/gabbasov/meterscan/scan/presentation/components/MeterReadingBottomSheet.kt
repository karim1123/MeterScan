package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.scan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterReadingBottomSheet(
    reading: String,
    onReadingChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetryScanning: () -> Unit,
    isLoading: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onRetryScanning,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.meter_reading),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reading,
                onValueChange = { newValue ->
                    // Фильтруем ввод, чтобы разрешить только цифры
                    if (newValue.all { it.isDigit() }) {
                        onReadingChange(newValue)
                    }
                },
                label = { Text(stringResource(R.string.enter_meter_reading)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onRetryScanning,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.try_again))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = reading.isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }

            // Добавляем отступ снизу для лучшего UX на устройствах с жестовой навигацией
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
