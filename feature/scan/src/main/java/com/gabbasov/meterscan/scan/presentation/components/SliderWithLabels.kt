package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SliderWithLabels(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = { newValue ->
                    onValueChange(newValue.roundToInt())
                },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = range.count() - 2,
                modifier = Modifier.weight(1.5f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = range.last.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun SliderWithLabelsPreview() {
    var value = 5
    MaterialTheme {
        SliderWithLabels(
            value = value,
            onValueChange = { value = it },
            range = 3..10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
