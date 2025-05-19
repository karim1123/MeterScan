package com.gabbasov.meterscan.work.presentation.list.pager.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.common.ui.components.meters.MeterTypeIcon
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheet(
    meter: Meter,
    onDismiss: () -> Unit,
    onBuildRoute: () -> Unit,
    onTakeReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Timber.d("MapBottomSheet: Showing sheet for meter ${meter.number}")

    ModalBottomSheet(
        onDismissRequest = {
            Timber.d("MapBottomSheet: Dismissing sheet for meter ${meter.number}")
            onDismiss()
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Информация о счетчике
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

                MeterTypeIcon(type = meter.type)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBuildRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Построить маршрут")
                }

                Button(
                    onClick = onTakeReading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Снять показания")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}