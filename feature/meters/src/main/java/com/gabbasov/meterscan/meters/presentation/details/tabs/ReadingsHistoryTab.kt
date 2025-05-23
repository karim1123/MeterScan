package com.gabbasov.meterscan.meters.presentation.details.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterReading
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.meters.presentation.details.calculateMonthlyConsumption
import com.gabbasov.meterscan.meters.presentation.details.components.ReadingListItem
import com.gabbasov.meterscan.meters.presentation.details.components.YearConsumptionChart
import com.gabbasov.meterscan.meters.presentation.details.getMeterUnits
import com.gabbasov.meterscan.meters.presentation.details.prepareMonthlyData
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ReadingsHistoryTab(meter: Meter) {
    val currentYear = LocalDate.now().year
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var showYearPicker by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<Month?>(null) }

    if (showYearPicker) {
        AlertDialog(
            onDismissRequest = { showYearPicker = false },
            title = { Text("Выберите год") },
            text = {
                Column {
                    // Генерируем список лет на основе показаний
                    val years = meter.readings
                        .map { it.date.year }
                        .distinct()
                        .sorted()

                    years.forEach { year ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedYear = year
                                    showYearPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                color = if (year == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYearPicker = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    val yearReadings = meter.readings.filter {
        it.date.year == selectedYear
    }.sortedBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showYearPicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выберите год",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Выбрать год"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Изменено название на год и месяц
                    Text(
                        text = if (selectedMonth != null) {
                            "${
                                selectedMonth!!.getDisplayName(
                                    TextStyle.FULL_STANDALONE,
                                    Locale("ru")
                                )
                            } $selectedYear"
                        } else {
                            "$selectedYear год"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (yearReadings.isEmpty()) {
                        Text(
                            text = "Нет данных за $selectedYear год",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        YearConsumptionChart(
                            modifier = Modifier.height(200.dp),
                            readings = yearReadings,
                            meterType = meter.type,
                            onMonthSelected = { month ->
                                selectedMonth = month
                            }
                        )
                    }

                    if (selectedMonth != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        val monthConsumption =
                            prepareMonthlyData(yearReadings)[selectedMonth] ?: 0.0
                        val monthReading = yearReadings
                            .filter { it.date.month == selectedMonth }
                            .maxByOrNull { it.date }

                        if (monthReading != null) {
                            Text(
                                text = "Показание за ${
                                    selectedMonth!!.getDisplayName(
                                        TextStyle.FULL,
                                        Locale("ru")
                                    )
                                }: ${monthReading.value.roundToInt()} ${getMeterUnits(meter.type)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Потребление: ${monthConsumption.roundToInt()} ${getMeterUnits(meter.type)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Список показаний по месяцам
        if (yearReadings.isNotEmpty()) {
            item {
                Text(
                    text = "Детализация по месяцам",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            items(yearReadings.sortedByDescending { it.date }) { reading ->
                val consumptionMap = calculateMonthlyConsumption(yearReadings)
                val consumption = consumptionMap[reading.date] ?: 0.0

                ReadingListItem(
                    reading = reading,
                    meterType = meter.type,
                    consumption = consumption
                )
                HorizontalDivider()
            }
        }
    }
}


@Preview
@Composable
fun ReadingsHistoryTabPreview() {
    val meter = Meter(
        id = "12321",
        address = "г. Москва, ул. Ленина, д. 1",
        owner = "Иванов И.И.",
        installationDate = LocalDate.of(2020, 1, 1),
        nextCheckDate = LocalDate.of(2025, 1, 1),
        notes = "Счетчик установлен в ванной комнате",
        type = MeterType.ELECTRICITY,
        number = "123456",
        readings = listOf(
            MeterReading(date = LocalDate.now(), value = 100.0),
            MeterReading(date = LocalDate.now().minusMonths(1), value = 90.0),
            MeterReading(date = LocalDate.now().minusMonths(2), value = 80.0)
        )
    )

    ReadingsHistoryTab(meter)
}