package com.gabbasov.meterscan.meters.presentation.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.meters.R
import com.gabbasov.meterscan.meters.presentation.details.tabs.AboutMeterTab
import com.gabbasov.meterscan.meters.presentation.details.tabs.ReadingsHistoryTab
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterReading
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.common.ui.components.meters.MeterTypeIcon
import com.gabbasov.meterscan.common.ui.dialog.LowerValueWarningDialog
import com.gabbasov.meterscan.common.ui.dialog.ReadingInputDialog
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeterDetailScreenRoute(
    coordinator: MeterDetailCoordinator,
    meterId: String
) = with(LocalContext.current) {
    val state by coordinator.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(meterId) {
        coordinator.loadMeter(meterId)
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorText.asText())
            }
        }
    }

    if (state.showReadingDialog) {
        ReadingInputDialog(
            lastReading = state.meter?.readings?.maxByOrNull { it.date }?.value,
            onDismiss = { coordinator.onDismissReadingDialog() },
            onSave = { coordinator.onSaveReading(it) }
        )
    }

    if (state.showLowerValueWarning) {
        LowerValueWarningDialog(
            reading = state.newReading,
            lastReading = state.meter?.readings?.maxByOrNull { it.date }?.value ?: 0.0,
            onConfirm = { coordinator.onConfirmLowerValue() },
            onEdit = { coordinator.onDismissLowerValueWarning() },
            onDismiss = { coordinator.onDismissLowerValueWarning() }
        )
    }

    LaunchedEffect(state.navigateToScan) {
        state.navigateToScan?.let { meterId ->
            coordinator.onNavigateToScan(meterId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали счетчика") },
                navigationIcon = {
                    IconButton(onClick = { coordinator.onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { coordinator.onAddReading() }) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = stringResource(R.string.add_reading)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.meter?.let { meter ->
                MeterDetailsTabs(
                    modifier = Modifier.padding(paddingValues),
                    meter = meter
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Счетчик не найден",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MeterDetailsTabs(
    modifier: Modifier = Modifier,
    meter: Meter
) {
    val tabs = listOf("О приборе", "История показаний")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val currentReading = meter.readings.maxByOrNull { it.date }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Верхний блок с заголовком и номером счетчика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "№ ${meter.number}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = getMeterTypeText(meter.type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        currentReading?.let { reading ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Текущие показания: ${reading.value.toInt()} ${getMeterUnits(meter.type)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Опционально можно добавить дату
                            Text(
                                text = "от ${reading.date.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                )}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MeterTypeIcon(
                        type = meter.type,
                        size = 40f
                    )
                }
            }
        }

        // Табы
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        // Содержимое табов
        when (selectedTabIndex) {
            0 -> AboutMeterTab(meter)
            1 -> ReadingsHistoryTab(meter)
        }
    }
}

@Preview
@Composable
private fun MeterDetailsTabsPreview() {
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
            MeterReading(date = LocalDate.now().minusMonths(1), value = 200.0)
        )
    )

    MeterDetailsTabs(
        meter = meter
    )
}