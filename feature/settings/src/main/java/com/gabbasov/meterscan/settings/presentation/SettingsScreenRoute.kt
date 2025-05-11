package com.gabbasov.meterscan.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabbasov.meterscan.designsystem.MeterScanTheme
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.settings.R
import com.gabbasov.meterscan.settings.presentation.components.NavigatorSelector
import com.gabbasov.meterscan.settings.presentation.components.ScanParametersSettings
import com.gabbasov.meterscan.settings.presentation.components.SettingsItem

@Composable
internal fun SettingsScreenRoute(
    coordinator: SettingsCoordinator
) {
    val uiState by coordinator.state.collectAsStateWithLifecycle()

    MeterScanTheme {
        SettingsScreen(
            state = uiState,
            onCameraModeChanged = coordinator::onCameraModeChanged,
            onNavigatorTypeChanged = coordinator::onNavigatorTypeChanged,
            onSignOutClicked = coordinator::onSignOutClicked,
            onBufferSizeChanged = coordinator::onBufferSizeChanged,
            onConfidenceThresholdChanged = coordinator::onConfidenceThresholdChanged,
            onHighConfidenceThresholdChanged = coordinator::onHighConfidenceThresholdChanged
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onCameraModeChanged: (Boolean) -> Unit,
    onNavigatorTypeChanged: (NavigatorType) -> Unit,
    onSignOutClicked: () -> Unit,
    onBufferSizeChanged: (Int) -> Unit,
    onConfidenceThresholdChanged: (Float) -> Unit,
    onHighConfidenceThresholdChanged: (Float) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.userEmail ?: stringResource(R.string.no_email),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSignOutClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.sign_out))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Переключатель режима камеры
                    SettingsItem(
                        title = stringResource(R.string.use_camera_mode),
                        subtitle = stringResource(R.string.use_camera_mode_description),
                        action = {
                            Switch(
                                checked = state.useCameraMode,
                                onCheckedChange = onCameraModeChanged
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Выбор навигатора
                    NavigatorSelector(
                        selectedNavigator = state.navigatorType,
                        onNavigatorSelected = onNavigatorTypeChanged
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ScanParametersSettings(
                bufferSize = state.bufferSize,
                confidenceThreshold = state.confidenceThreshold,
                highConfidenceThreshold = state.highConfidenceThreshold,
                onBufferSizeChanged = { size ->
                    onBufferSizeChanged(size)
                },
                onConfidenceThresholdChanged = { threshold ->
                    onConfidenceThresholdChanged(threshold)
                },
                onHighConfidenceThresholdChanged = { threshold ->
                    onHighConfidenceThresholdChanged(threshold)
                }
            )

            // Версия приложения
            Text(
                text = stringResource(R.string.app_version, state.appVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MeterScanTheme {
        SettingsScreen(
            state = SettingsState(
                userEmail = "user@example.com",
                useCameraMode = true,
                navigatorType = NavigatorType.GOOGLE_MAPS,
                appVersion = "1.0.0"
            ),
            onCameraModeChanged = {},
            onNavigatorTypeChanged = {},
            onSignOutClicked = {},
            onBufferSizeChanged = {},
            onConfidenceThresholdChanged = {},
            onHighConfidenceThresholdChanged = {},
        )
    }
}