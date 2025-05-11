package com.gabbasov.meterscan.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.settings.R

@Composable
fun ScanParametersSettings(
    bufferSize: Int,
    confidenceThreshold: Float,
    highConfidenceThreshold: Float,
    onBufferSizeChanged: (Int) -> Unit,
    onConfidenceThresholdChanged: (Float) -> Unit,
    onHighConfidenceThresholdChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.scan_parameters_title),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.buffer_size_label, bufferSize),
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = bufferSize.toFloat(),
            onValueChange = { onBufferSizeChanged(it.toInt()) },
            valueRange = 5f..30f,
            steps = 25,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.buffer_size_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.confidence_threshold_label, confidenceThreshold),
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = confidenceThreshold,
            onValueChange = onConfidenceThresholdChanged,
            valueRange = 0.3f..0.7f,
            steps = 40,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.confidence_threshold_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.high_confidence_threshold_label, highConfidenceThreshold),
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = highConfidenceThreshold,
            onValueChange = onHighConfidenceThresholdChanged,
            valueRange = 0.75f..0.95f,
            steps = 20,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.high_confidence_threshold_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
