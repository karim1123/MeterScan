package com.gabbasov.meterscan.work.presentation.list.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.work.presentation.components.WorkMeterListItem
import com.gabbasov.meterscan.work.presentation.list.WorkState

@Composable
internal fun WorkMetersList(
    state: WorkState,
    onMeterClick: (String) -> Unit,
    onTakeReading: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedMeters = state.filteredMeters.filter { it.state != MeterState.NOT_REQUIRED }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        displayedMeters.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Нет счетчиков, требующих снятия показаний",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(displayedMeters, key = { it.id }) { meter ->
                    WorkMeterListItem(
                        meter = meter,
                        onClick = { onMeterClick(meter.id) },
                        onTakeReading = { onTakeReading(meter.id) }
                    )
                }
            }
        }
    }
}
