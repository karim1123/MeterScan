package com.gabbasov.meterscan.scan.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.scan.R
import com.gabbasov.meterscan.scan.presentation.components.MeterInfoCard
import com.gabbasov.meterscan.scan.presentation.components.SliderWithLabels
import com.gabbasov.meterscan.scan.presentation.components.picker.ListPicker
import com.gabbasov.meterscan.scan.presentation.components.picker.integerOnlyRegex
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun MeterReadingBottomSheet(
    reading: String,
    isScanning: Boolean,
    onReadingChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetryScanning: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    defaultDigitCount: Int,
    selectedMeter: Meter? = null,
    onSelectMeter: () -> Unit
) {
    // Инициализируем digitCount на основе длины reading или defaultDigitCount
    var digitCount by remember(reading) {
        mutableIntStateOf(
            if (reading.isNotEmpty()) reading.length else defaultDigitCount
        )
    }

    var digitValues by remember { mutableStateOf(List(digitCount) { 0 }) }
    val digitsList = remember { (0..9).toList() }

    // Обновляем digitCount при изменении reading
    LaunchedEffect(reading) {
        if (reading.isNotEmpty()) {
            val newDigitCount = reading.length
            if (newDigitCount != digitCount) {
                digitCount = newDigitCount
            }

            digitValues = reading
                .take(digitCount)
                .map { it.digitToIntOrNull() ?: 0 }
        }
    }

    // Обновление digitValues при изменении digitCount
    LaunchedEffect(digitCount) {
        digitValues = if (digitCount > digitValues.size) {
            digitValues + List(digitCount - digitValues.size) { 0 }
        } else {
            digitValues.take(digitCount)
        }

        // Обновляем reading только если он пустой или его длина не соответствует digitCount
        if (reading.isEmpty() || reading.length != digitCount) {
            onReadingChange(digitValues.joinToString(""))
        }
    }

    // Обновление reading при изменении digitValues
    LaunchedEffect(digitValues) {
        onReadingChange(digitValues.joinToString(""))
    }

    val sheetState = rememberFlexibleBottomSheetState(
        flexibleSheetSize = FlexibleSheetSize(
            intermediatelyExpanded = 0.5f,
            fullyExpanded = 0.88f,
        ),
        skipSlightlyExpanded = true,
        allowNestedScroll = false
    )

    val scope = rememberCoroutineScope()
    LaunchedEffect(isScanning) {
        scope.launch {
            if (isScanning) {
                sheetState.intermediatelyExpand()
            } else {
                sheetState.fullyExpand()
            }
        }
    }

    FlexibleBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissBottomSheet
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

            // Отображение выбранного счетчика или кнопки для выбора
            if (selectedMeter != null) {
                MeterInfoCard(
                    meter = selectedMeter,
                    onClick = onSelectMeter
                )
            } else {
                OutlinedButton(
                    onClick = onSelectMeter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выбрать счетчик")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода
            OutlinedTextField(
                value = reading,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        onReadingChange(newValue)
                        // Обновляем барабанчики
                        val newDigitCount = newValue.length
                        if (newDigitCount != digitCount && newDigitCount > 0) {
                            digitCount = newDigitCount
                        }

                        digitValues = newValue
                            .padStart(digitCount, '0')
                            .take(digitCount)
                            .map { it.digitToIntOrNull() ?: 0 }
                    }
                },
                label = { Text(stringResource(R.string.enter_meter_reading)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedMeter != null // Кнопка активна только если выбран счетчик
            ) {
                Text(stringResource(R.string.save))
            }

            // Остальной код для барабанчиков и слайдера
            if (!isScanning) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Барабанчики для редактирования
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Редактировать показания",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ряд барабанчиков
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            digitValues.forEachIndexed { index, value ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Используем key для обновления при изменении value
                                    key(value) {
                                        ListPicker(
                                            initialValue = value,
                                            values = digitsList,
                                            onValueChange = { newValue ->
                                                val newDigitValues = digitValues.toMutableList()
                                                newDigitValues[index] = newValue
                                                digitValues = newDigitValues
                                            },
                                            wrapSelectorWheel = true,
                                            format = { this.toString() },
                                            parse = {
                                                try {
                                                    takeIf { it.matches(integerOnlyRegex) }?.toInt()
                                                } catch (_: NumberFormatException) {
                                                    null
                                                }
                                            },
                                            enableEdition = false,
                                            beyondViewportPageCount = 1,
                                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                                textAlign = TextAlign.Center
                                            ),
                                            verticalPadding = 8.dp,
                                            keyboardType = KeyboardType.Number,
                                            modifier = Modifier.height(120.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Слайдер для количества цифр
                SliderWithLabels(
                    value = digitCount,
                    onValueChange = { newCount -> digitCount = newCount },
                    range = 1..8,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
fun MeterReadingBottomSheetWithPickerPreview() {
    MaterialTheme {
        var reading by remember { mutableStateOf("123456") }
        MeterReadingBottomSheet(
            reading = reading,
            isScanning = false,
            onReadingChange = { reading = it },
            onSave = {},
            onRetryScanning = {},
            onDismissBottomSheet = {},
            defaultDigitCount = 4,
            selectedMeter = null,
            onSelectMeter = {}
        )
    }
}