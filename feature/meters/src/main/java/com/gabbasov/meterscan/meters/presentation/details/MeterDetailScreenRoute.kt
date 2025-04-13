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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.meters.domain.Meter
import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import com.gabbasov.meterscan.meters.presentation.components.MeterTypeIcon
import com.gabbasov.meterscan.meters.presentation.details.tabs.AboutMeterTab
import com.gabbasov.meterscan.meters.presentation.details.tabs.ReadingsHistoryTab
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeterDetailScreenRoute(
    coordinator: MeterDetailCoordinator,
    meterId: String
) = with(LocalContext.current) {
    val state by coordinator.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление счетчика") },
            text = { Text("Вы уверены, что хотите удалить этот счетчик?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        coordinator.onDeleteMeter()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
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
               /* actions = {
                    IconButton(onClick = { coordinator.onEditMeter() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }*/
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
                RedesignedMeterDetailScreen(
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
private fun RedesignedMeterDetailScreen(
    modifier: Modifier = Modifier,
    meter: Meter
) {
    val tabs = listOf("О приборе", "История показаний")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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