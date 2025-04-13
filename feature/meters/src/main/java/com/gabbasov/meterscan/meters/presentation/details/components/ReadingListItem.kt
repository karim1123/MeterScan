package com.gabbasov.meterscan.meters.presentation.details.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import com.gabbasov.meterscan.meters.presentation.details.getMeterUnits
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReadingListItem(
    reading: MeterReading,
    meterType: MeterType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = reading.date.format(
                    DateTimeFormatter.ofPattern(
                        "dd MMMM yyyy",
                        Locale("ru")
                    )
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Статус: Приняты",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
        }

        Text(
            text = "${reading.value} ${getMeterUnits(meterType)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReadingListItemPreview() {
    MaterialTheme {
        ReadingListItem(
            reading = MeterReading(
                date = java.time.LocalDate.now(),
                value = 123.45,
            ),
            meterType = MeterType.ELECTRICITY
        )
    }
}
