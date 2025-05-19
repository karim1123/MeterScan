package com.gabbasov.meterscan.work.presentation.list.pager.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gabbasov.meterscan.common.R
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

private const val ANIMATION_DURATION = 0.3f

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WorkMapTab(
    meters: List<Meter>,
    userLocation: Pair<Double, Double>?, // широта, долгота
    navigatorType: NavigatorType,
    onRequestLocation: () -> Unit,
    onBuildRoute: (Meter) -> Unit,
    onTakeReading: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedMeter by remember { mutableStateOf<Meter?>(null) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && userLocation == null) {
            onRequestLocation()
        }
    }

    // Состояние для MapView
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<Map?>(null) }


    Box(modifier = modifier.fillMaxSize()) {
        // Отображение карты
        AndroidView(
            factory = { ctx ->
                MapKitFactory.getInstance().onStart()
                MapView(ctx).apply {
                    mapView = this
                    map = this.mapWindow.map

                    // Устанавливаем начальное положение камеры (если есть местоположение пользователя)
                    userLocation?.let { (latitude, longitude) ->
                        this.mapWindow.map.move(
                            CameraPosition(
                                Point(latitude, longitude),
                                15.0f, 0.0f, 0.0f
                            ),
                            Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
                            null
                        )
                    }
                }
            },
            update = { view ->
                // Удаление предыдущих объектов с карты
                view.mapWindow.map.mapObjects.clear()

                // Добавление маркеров счетчиков на карту
                meters.forEach { meter ->
                    meter.address.latitude?.let { lat ->
                        meter.address.longitude?.let { lon ->
                            val point = Point(lat, lon)
                            val iconResId = when (meter.type) {
                                MeterType.ELECTRICITY -> R.drawable.ic_electricity
                                MeterType.WATER -> R.drawable.ic_water
                                MeterType.GAS -> R.drawable.ic_gas
                            }

                            // Используем правильную сигнатуру метода addPlacemark
                            val placemarkObject = view.mapWindow.map.mapObjects.addPlacemark(
                                point,
                                ImageProvider.fromResource(context, iconResId)
                            )

                            // Добавляем обработчик нажатия
                            placemarkObject.addTapListener(MapObjectTapListener { _, _ ->
                                selectedMeter = meter
                                true
                            })
                        }
                    }
                }
            },
            onRelease = {
                mapView?.onStop()
                MapKitFactory.getInstance().onStop()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Если нет разрешения на местоположение, показываем кнопку запроса
        if (!locationPermissionState.status.isGranted) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Для отображения карты требуется доступ к местоположению")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                        Text("Предоставить доступ")
                    }
                }
            }
        }

        // Bottom Sheet для выбранного счетчика
        if (selectedMeter != null) {
            MapBottomSheet(
                meter = selectedMeter!!,
                onDismiss = { selectedMeter = null },
                onBuildRoute = {
                    selectedMeter?.let { meter ->
                        buildRoute(context, meter, userLocation, navigatorType)
                        onBuildRoute(meter)
                    }
                },
                onTakeReading = {
                    selectedMeter?.let { meter ->
                        onTakeReading(meter.id)
                    }
                }
            )
        }
    }
}

// Флаг для отслеживания инициализации MapKit
private var isMapKitInitialized = false

// Функция для построения маршрута в навигаторе
private fun buildRoute(
    context: Context,
    meter: Meter,
    userLocation: Pair<Double, Double>?,
    navigatorType: NavigatorType
) {
    val destLat = meter.address.latitude ?: return
    val destLon = meter.address.longitude ?: return

    val sourceLat = userLocation?.first ?: return
    val sourceLon = userLocation.second

    val uri = when (navigatorType) {
        NavigatorType.GOOGLE_MAPS -> {
            // URI для Google Maps
            Uri.parse("google.navigation:q=$destLat,$destLon")
        }
        NavigatorType.YANDEX_MAPS -> {
            // URI для Яндекс Навигатора
            Uri.parse("yandexnavi://build_route_on_map?lat_to=$destLat&lon_to=$destLon&lat_from=$sourceLat&lon_from=$sourceLon")
        }
        NavigatorType.SYSTEM_DEFAULT -> {
            // URI для системного навигатора
            Uri.parse("geo:$destLat,$destLon?q=$destLat,$destLon(${meter.address.street})")
        }
    }

    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Обработка ошибок (например, если требуемый навигатор не установлен)
        // В этом случае можно открыть системный навигатор как запасной вариант
        if (navigatorType != NavigatorType.SYSTEM_DEFAULT) {
            val defaultUri = Uri.parse("geo:$destLat,$destLon?q=$destLat,$destLon(${meter.address.street})")
            val defaultIntent = Intent(Intent.ACTION_VIEW, defaultUri)
            defaultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                context.startActivity(defaultIntent)
            } catch (e: Exception) {
                // Ничего не делаем, если и это не сработало
            }
        }
    }
}