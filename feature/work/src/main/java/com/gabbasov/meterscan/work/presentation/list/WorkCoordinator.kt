package com.gabbasov.meterscan.work.presentation.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.gabbasov.meterscan.model.meter.Meter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.androidx.compose.koinViewModel

internal class WorkCoordinator(
    private val viewModel: WorkViewModel,
    private val navController: NavHostController,
    private val context: Context
) {
    val state = viewModel.uiState

    // Клиент для получения местоположения
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun onMeterSelected(meterId: String) {
        // Пока отключаем навигацию к деталям счетчика с карты
        // навигация работает из списка
    }

    fun onSearchQueryChanged(query: String) {
        viewModel.execute(WorkAction.SearchMeters(query))
    }

    fun onTakeReading(meterId: String) {
        viewModel.execute(WorkAction.ShowReadingDialog(meterId))
    }

    fun onNavigationHandled() {
        viewModel.execute(WorkAction.NavigationHandled)
    }

    fun onSaveReading(reading: String) {
        viewModel.execute(WorkAction.SaveReading(reading))
    }

    fun onConfirmLowerValue() {
        viewModel.execute(WorkAction.ConfirmLowerValue)
    }

    fun onDismissReadingDialog() {
        viewModel.execute(WorkAction.DismissReadingDialog)
    }

    fun onDismissLowerValueWarning() {
        viewModel.execute(WorkAction.DismissLowerValueWarning)
    }

    // Методы для работы с картой

    @SuppressLint("MissingPermission")
    fun requestUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    viewModel.execute(
                        WorkAction.SetUserLocation(
                            Pair(it.latitude, it.longitude)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Обработка ошибки получения местоположения
        }
    }

    fun onBuildRoute(meter: Meter) {
        viewModel.execute(WorkAction.BuildRoute(meter))
    }

    fun onTabSelected(index: Int) {
        viewModel.execute(WorkAction.SetSelectedTabIndex(index))
    }
}

@Composable
internal fun rememberWorkMetersListCoordinator(
    viewModel: WorkViewModel = koinViewModel(),
    navController: NavHostController,
): WorkCoordinator {
    val context = LocalContext.current

    return remember(viewModel, navController, context) {
        WorkCoordinator(
            viewModel = viewModel,
            navController = navController,
            context = context
        )
    }
}
