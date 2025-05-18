package com.gabbasov.meterscan.scan.presentation

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.scan.R
import com.gabbasov.meterscan.scan.domain.DigitBox
import com.gabbasov.meterscan.scan.presentation.components.CameraView
import com.gabbasov.meterscan.scan.presentation.components.DigitOverlayView
import com.gabbasov.meterscan.scan.presentation.components.FlashlightControl
import com.gabbasov.meterscan.scan.presentation.components.bottomsheet.MeterReadingBottomSheet
import com.gabbasov.meterscan.scan.presentation.components.RecognitionProgressIndicator
import com.gabbasov.meterscan.scan.presentation.dialog.MeterSelectionDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
internal fun MeterScanScreenRoute(
    coordinator: MeterScanCoordinator,
    meterId: String? = null
) {
    val uiState by coordinator.state.collectAsStateWithLifecycle()

    // Загружаем счетчик при запуске экрана, если передан ID
    LaunchedEffect(meterId) {
        coordinator.loadMeter(meterId)
    }

    MeterScanTheme {
        MeterScanScreen(
            state = uiState,
            onDigitsDetected = coordinator::onDigitsDetected,
            onReadingUpdated = coordinator::onReadingUpdated,
            onSaveReading = coordinator::onSaveReading,
            onRetryScanning = coordinator::onRetryScanning,
            onDismissBottomSheet = coordinator::onDismissBottomSheet,
            confidenceThreshold = coordinator.getConfidenceThreshold(),
            highConfidenceThreshold = coordinator.getHighConfidenceThreshold(),
            onToggleFlashlight = coordinator::onToggleFlashlight,
            onRotateCamera = coordinator::onRotateCamera,
            onTogglePause = coordinator::onTogglePause,
            onShowMeterSelection = coordinator::onShowMeterSelection,
            onHideMeterSelection = coordinator::onHideMeterSelection,
            navigateToMetersList = coordinator::navigateToMetersList
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
internal fun MeterScanScreen(
    state: MeterScanState,
    onDigitsDetected: (List<DigitBox>) -> Unit,
    onReadingUpdated: (String) -> Unit,
    onSaveReading: () -> Unit,
    onRetryScanning: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    confidenceThreshold: Float,
    highConfidenceThreshold: Float,
    onToggleFlashlight: () -> Unit,
    onRotateCamera: () -> Unit,
    onTogglePause: () -> Unit,
    onShowMeterSelection: () -> Unit,
    onHideMeterSelection: () -> Unit,
    navigateToMetersList: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val overlayView = remember { DigitOverlayView(context) }

    LaunchedEffect(state.detectedDigits, state.cameraRotation) {
        if (state.detectedDigits.isNotEmpty()) {
            overlayView.setResults(state.detectedDigits, state.cameraRotation)
        } else {
            overlayView.clear()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    if (state.showSuccessMessage) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.meter_reading_saved),
                duration = SnackbarDuration.Short
            )
        }
    }

    var isFlashlightOn by remember { mutableStateOf(false) }
    val flashlightControl = remember { mutableStateOf<FlashlightControl?>(null) }
    var cameraView by remember { mutableStateOf<CameraView?>(null) }

    if (state.showMeterSelection) {
        MeterSelectionDialog(
            onDismiss = onHideMeterSelection,
            navigateToMetersList = navigateToMetersList
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_meter_reading)) },
                actions = {
                    IconButton(onClick = onTogglePause) {
                        val imageRes = if (state.isPaused) R.drawable.ic_play else R.drawable.ic_pause
                        Icon(
                            imageVector = ImageVector.vectorResource(id = imageRes),
                            contentDescription = stringResource(
                                if (state.isPaused)
                                    R.string.resume_scanning
                                else
                                    R.string.pause_scanning
                            )
                        )
                    }

                    IconButton(onClick = onRotateCamera) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_rotate),
                            contentDescription = stringResource(R.string.rotate_camera)
                        )
                    }

                    IconButton(
                        onClick = {
                            flashlightControl.value?.let {
                                isFlashlightOn = it.toggleFlashlight()
                                onToggleFlashlight()
                            }
                        }
                    ) {
                        val iconRes = if (state.flashlightEnabled) R.drawable.flash_on else R.drawable.flash_off
                        Icon(
                            imageVector = ImageVector.vectorResource(id = iconRes),
                            contentDescription = if (state.flashlightEnabled)
                                stringResource(R.string.turn_off_flashlight) else stringResource(R.string.turn_on_flashlight)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.showBottomSheet) {
                MeterReadingBottomSheet(
                    reading = state.meterReading,
                    isScanning = !state.isPaused,
                    onReadingChange = onReadingUpdated,
                    onSave = onSaveReading,
                    onRetryScanning = onRetryScanning,
                    onDismissBottomSheet = onDismissBottomSheet,
                    defaultDigitCount = state.meterReading.length,
                    selectedMeter = state.selectedMeter,
                    onSelectMeter = onShowMeterSelection
                )
            }

            if (cameraPermissionState.status.isGranted) {
                AndroidView(
                    factory = { ctx ->
                        val view = CameraView(
                            context = ctx,
                            lifecycleOwner = lifecycleOwner,
                            onDigitsDetected = onDigitsDetected,
                            confidenceThreshold = confidenceThreshold,
                            highConfidenceThreshold = highConfidenceThreshold,
                            rotation = state.cameraRotation,
                            isPaused = state.isPaused
                        )
                        flashlightControl.value = view
                        cameraView = view
                        view
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        view.setPaused(state.isPaused)
                        if (view.rotation != state.cameraRotation) {
                            view.rotation = state.cameraRotation
                        }
                    }
                )

                if (!state.isPaused) {
                    AndroidView(
                        factory = { overlayView },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (state.isScanning) {
                    RecognitionProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        progress = state.recognitionProgress,
                        stabilityScore = state.stabilityScore
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center,
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
