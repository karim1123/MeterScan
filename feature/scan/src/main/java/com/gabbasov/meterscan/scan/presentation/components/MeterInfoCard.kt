package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterReading
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.ui.components.meters.MeterTypeIcon
import java.time.LocalDate

@Composable
fun MeterInfoCard(
    meter: Meter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MeterTypeIcon(type = meter.type, size = 40f)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "№ ${meter.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = meter.address.getFullAddress(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun MeterInfoCardPreview() {
    val meter = Meter(
        id = "12321",
        address = Address(
            street = "Улица Пушкина, дом 1",
            latitude = 55.7558,
            longitude = 37.6173
        ),
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
    MeterInfoCard(
        meter = meter,
        onClick = {}
    )
}