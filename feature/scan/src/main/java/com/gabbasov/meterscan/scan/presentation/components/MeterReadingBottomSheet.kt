package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.scan.R
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState

@Composable
fun MeterReadingBottomSheet(
    reading: String,
    onReadingChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetryScanning: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    isLoading: Boolean
) {
    FlexibleBottomSheet(
        sheetState = rememberFlexibleBottomSheetState(
            flexibleSheetSize = FlexibleSheetSize(
                fullyExpanded = 0.6f,
                intermediatelyExpanded = 0.33f,
                slightlyExpanded = 0.2f
            )
        ),
        onDismissRequest = onDismissBottomSheet,
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp),
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
        }
    }
}