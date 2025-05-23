package com.gabbasov.meterscan.scan.presentation

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.scan.R
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.scan.presentation.components.CameraView
import com.gabbasov.meterscan.scan.presentation.components.DigitOverlayView
import com.gabbasov.meterscan.scan.presentation.components.ErrorDialog
import com.gabbasov.meterscan.scan.presentation.components.MeterReadingBottomSheet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
internal fun MeterScanScreenRoute(
    coordinator: MeterScanCoordinator
) {
    val uiState by coordinator.state.collectAsStateWithLifecycle()

    MeterScanTheme {
        MeterScanScreen(
            state = uiState,
            onDigitsDetected = coordinator::onDigitsDetected,
            onReadingUpdated = coordinator::onReadingUpdated,
            onSaveReading = coordinator::onSaveReading,
            onRetryScanning = coordinator::onRetryScanning,
            onEnterManually = coordinator::onEnterManually,
            onDismissBottomSheet = coordinator::onDismissBottomSheet,
            onDismissErrorDialog = coordinator::onDismissErrorDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
internal fun MeterScanScreen(
    state: MeterScanState,
    onDigitsDetected: (List<DigitBox>) -> Unit,
    onReadingUpdated: (String) -> Unit,
    onSaveReading: (String) -> Unit,
    onRetryScanning: () -> Unit,
    onEnterManually: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onDismissErrorDialog: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Запрос разрешения на камеру
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // Создаем и запоминаем DigitOverlayView для наложения поверх камеры
    val overlayView = remember { DigitOverlayView(context) }

    // Отображаем текущие обнаруженные цифры
    LaunchedEffect(state.detectedDigits) {
        if (state.detectedDigits.isNotEmpty()) {
            overlayView.setResults(state.detectedDigits)
        } else {
            overlayView.clear()
        }
    }

    // Окно подтверждения для показаний счетчика
    if (state.showBottomSheet) {
        MeterReadingBottomSheet(
            reading = state.meterReading,
            onReadingChange = onReadingUpdated,
            onSave = { onSaveReading(state.meterReading) },
            onDismiss = onDismissBottomSheet,
            isLoading = state.isLoading
        )
    }

    // Диалог ошибки при неудачном распознавании
    if (state.showErrorDialog) {
        /*ErrorDialog(
            onRetry = onRetryScanning,
            onEnterManually = onEnterManually,
            onDismiss = onDismissErrorDialog
        )*/
    }

    // Показываем снекбар с успешным сохранением
    val snackbarHostState = remember { SnackbarHostState() }
    if (state.showSuccessMessage) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.meter_reading_saved),
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_meter_reading)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Отображаем превью камеры
                AndroidView(
                    factory = { ctx ->
                        CameraView(
                            context = ctx,
                            lifecycleOwner = lifecycleOwner,
                            onDigitsDetected = onDigitsDetected
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Накладываем слой с обнаруженными цифрами
                AndroidView(
                    factory = { overlayView },
                    modifier = Modifier.fillMaxSize()
                )

                // Индикатор сканирования
                if (state.isScanning) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            } else {
                // Запрос разрешения на использование камеры
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.camera_permission_required),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }
}
