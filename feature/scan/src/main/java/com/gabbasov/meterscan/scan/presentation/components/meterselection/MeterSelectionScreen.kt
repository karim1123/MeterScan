package com.gabbasov.meterscan.scan.presentation.components.meterselection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.scan.R
import com.gabbasov.meterscan.scan.presentation.components.MeterInfoCard
import com.gabbasov.meterscan.common.ui.components.meters.SearchTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterSelectionScreen(
    meters: List<Meter>,
    onMeterSelected: (Meter) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredMeters = remember(searchQuery, meters) {
        if (searchQuery.isBlank()) {
            meters
        } else {
            val query = searchQuery.lowercase()
            meters.filter { meter ->
                meter.number.lowercase().contains(query) ||
                        meter.address.getFullAddress().lowercase().contains(query) ||
                        meter.owner.lowercase().contains(query) ||
                        meter.type.name.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_meter_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поле поиска
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredMeters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            "Счетчики не найдены"
                        else
                            "Нет доступных счетчиков"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMeters) { meter ->
                        MeterInfoCard(
                            meter = meter,
                            onClick = { onMeterSelected(meter) }
                        )
                    }
                }
            }
        }
    }
}
