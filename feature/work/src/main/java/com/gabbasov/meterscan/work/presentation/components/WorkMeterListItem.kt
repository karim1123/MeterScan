package com.gabbasov.meterscan.work.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.ui.components.meters.MeterTypeIcon

@Composable
internal fun WorkMeterListItem(
    meter: Meter,
    onClick: () -> Unit,
    onTakeReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "№ ${meter.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meter.address.getFullAddress(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Последнее показание, если есть
                meter.readings.maxByOrNull { it.date }?.let { lastReading ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Текущее: ${lastReading.value.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Индикатор состояния
            if (meter.state != MeterState.NOT_REQUIRED) {
                MeterStateIndicator(
                    state = meter.state,
                    onTakeReading = onTakeReading
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MeterTypeIcon(type = meter.type)
        }
    }
}

@Preview
@Composable
private fun WorkMeterListItemPreview() {
    MaterialTheme {
        WorkMeterListItem(
            meter = Meter(
                id = "1",
                type = com.gabbasov.meterscan.model.meter.MeterType.ELECTRICITY,
                number = "12345678",
                address = com.gabbasov.meterscan.model.meter.Address(
                    street = "Улица Пушкина, дом 1"
                ),
                owner = "Иванов И.И.",
                readings = emptyList(),
                installationDate = java.time.LocalDate.now(),
                nextCheckDate = java.time.LocalDate.now()
            ),
            onClick = {},
            onTakeReading = {}
        )
    }
}
