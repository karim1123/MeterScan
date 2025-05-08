package com.gabbasov.meterscan.meters.presentation.details.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterReading
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.meters.presentation.details.components.InfoRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AboutMeterTab(meter: Meter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Основная информация",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Адрес:", value = meter.address)
                InfoRow(label = "Владелец:", value = meter.owner)
                InfoRow(
                    label = "Дата установки:",
                    value = meter.installationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                )
                InfoRow(
                    label = "Дата следующей поверки:",
                    value = meter.nextCheckDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                )
                meter.notes?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Примечания:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AboutMeterTabPreview() {
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
            MeterReading(date = LocalDate.now().minusMonths(1), value = 200.0)
        )
    )
    AboutMeterTab(meter)
}

