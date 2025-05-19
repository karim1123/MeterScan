package com.gabbasov.meterscan.work.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.MeterState

@Composable
internal fun MeterStateIndicator(
    state: MeterState,
    onTakeReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        MeterState.NOT_REQUIRED -> {

        }
        MeterState.REQUIRED -> {
            Row(
                modifier = Modifier
                    .clickable { onTakeReading() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Снять показания",
                )
            }
        }
        MeterState.SUBMITTED_TO_SERVER -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Отправлено",
                    tint = Color(0xFF4CAF50)
                )

            }
        }
        MeterState.SAVED_LOCALLY -> {
            Button(
                onClick = onTakeReading,
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA000)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Отправить",
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun MeterStateIndicatorPreview() {
    Column {
        MeterStateIndicator(
            state = MeterState.REQUIRED,
            onTakeReading = {}
        )
        MeterStateIndicator(
            state = MeterState.SAVED_LOCALLY,
            onTakeReading = {}
        )
        MeterStateIndicator(
            state = MeterState.SUBMITTED_TO_SERVER,
            onTakeReading = {}
        )
    }

}
