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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.work.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlin.math.log2

private const val ANIMATION_DURATION = 0.3f
private const val MIN_ZOOM = 10f
private const val MAX_ZOOM = 18f
private const val BASE_ZOOM = 15f

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
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<Map?>(null) }
    var previousZoom by remember { mutableStateOf(15.0f) }
    var isMapInitialized by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && userLocation == null) {
            onRequestLocation()
        }
    }

    LaunchedEffect(mapView, meters, userLocation) {
        if (mapView != null && !isMapInitialized && meters.isNotEmpty()) {
            delay(500) // Задержка 500мс для инициализации карты

            val visiblePoints = mutableListOf<Point>()

            // Собираем все точки счетчиков
            meters.forEach { meter ->
                meter.address.latitude?.let { lat ->
                    meter.address.longitude?.let { lon ->
                        visiblePoints.add(Point(lat, lon))
                    }
                }
            }

            // Добавляем текущее местоположение пользователя
            userLocation?.let { (lat, lon) ->
                visiblePoints.add(Point(lat, lon))
            }

            if (visiblePoints.isNotEmpty()) {
                // Найдем минимальные и максимальные координаты
                var minLat = Double.MAX_VALUE
                var maxLat = Double.MIN_VALUE
                var minLon = Double.MAX_VALUE
                var maxLon = Double.MIN_VALUE

                visiblePoints.forEach { point ->
                    minLat = minOf(minLat, point.latitude)
                    maxLat = maxOf(maxLat, point.latitude)
                    minLon = minOf(minLon, point.longitude)
                    maxLon = maxOf(maxLon, point.longitude)
                }

                // Центральная точка
                val centerLat = (minLat + maxLat) / 2
                val centerLon = (minLon + maxLon) / 2

                // Вычисляем оптимальный зум
                val latDelta = maxLat - minLat
                val lonDelta = maxLon - minLon

                // Коэффициент для запаса по краям (уменьшаем зум)
                val padding = 0.5
                val zoomLat = log2(360.0 / (latDelta * padding)) - 1
                val zoomLon = log2(360.0 / (lonDelta * padding)) - 1

                // Берем минимальный зум из двух направлений для отображения всех точек
                val optimalZoom = minOf(zoomLat, zoomLon).toFloat().coerceIn(MIN_ZOOM, MAX_ZOOM)

                // Перемещаем камеру на оптимальный зум
                map?.move(
                    CameraPosition(
                        Point(centerLat, centerLon),
                        optimalZoom,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
                    null
                )

                isMapInitialized = true
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Отображение карты
        AndroidView(
            factory = { ctx ->
                MapKitFactory.getInstance().onStart()
                MapView(ctx).apply {
                    mapView = this
                    map = this.mapWindow.map

                    // Устанавливаем начальное положение камеры
                    userLocation?.let { (latitude, longitude) ->
                        this.mapWindow.map.move(
                            CameraPosition(
                                Point(latitude, longitude),
                                BASE_ZOOM, 0.0f, 0.0f
                            ),
                            Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
                            null
                        )
                    }

                    // Правильная сигнатура с 4 параметрами
                    this.mapWindow.map.addCameraListener { _, cameraPosition, _, finished ->
                        if (finished) {
                            previousZoom = cameraPosition.zoom
                        }
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

                            // Создаем bitmap
                            val bitmap = MapIconUtils.createMeterIconBitmap(context, iconResId, meter.type)

                            // Масштабирование через IconStyle
                            val iconStyle = IconStyle().apply {
                                this.scale = 0.8f
                                this.zIndex = 0.5f
                            }

                            val placemarkObject = view.mapWindow.map.mapObjects.addPlacemark(
                                point,
                                ImageProvider.fromBitmap(bitmap),
                                iconStyle
                            )

                            placemarkObject.addTapListener(MapObjectTapListener { _, _ ->
                                selectedMeter = meter
                                true
                            })
                        }
                    }
                }

                userLocation?.let { (latitude, longitude) ->
                    val locationBitmap = MapIconUtils.createLocationMarkerBitmap(context)
                    val zoom = view.mapWindow.map.cameraPosition.zoom
                    val locationScale = 0.7f + (zoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM) * 0.8f

                    val locationStyle = IconStyle().apply {
                        this.scale = locationScale.coerceIn(0.7f, 1.5f)
                        this.zIndex = 1.0f
                    }

                    view.mapWindow.map.mapObjects.addPlacemark(
                        Point(latitude, longitude),
                        ImageProvider.fromBitmap(locationBitmap),
                        locationStyle
                    )
                }
            },
            onRelease = {
                mapView?.onStop()
                MapKitFactory.getInstance().onStop()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (locationPermissionState.status.isGranted) {
            FloatingActionButton(
                onClick = {
                    userLocation?.let { (latitude, longitude) ->
                        map?.move(
                            CameraPosition(
                                Point(latitude, longitude),
                                17.0f, 0.0f, 0.0f
                            ),
                            Animation(Animation.Type.SMOOTH, ANIMATION_DURATION),
                            null
                        )
                    } ?: onRequestLocation()
                },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_current_location),
                    contentDescription = "Мое местоположение",
                    tint = Color.Black
                )
            }
        }

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