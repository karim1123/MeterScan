package com.gabbasov.meterscan.work.presentation.list.pager.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.log2

private const val ANIMATION_DURATION = 0.3f
private const val MIN_ZOOM = 10f
private const val MAX_ZOOM = 18f
private const val BASE_ZOOM = 15f
private const val MIN_TAP_INTERVAL = 500L // Минимальный интервал между нажатиями в миллисекундах

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
    val coroutineScope = rememberCoroutineScope()

    // Используем rememberSaveable для сохранения состояния при переключении экранов
    var selectedMeter by rememberSaveable { mutableStateOf<Meter?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<Map?>(null) }
    var previousZoom by remember { mutableStateOf(15.0f) }
    var isMapInitialized by remember { mutableStateOf(false) }
    var lastSelectedMeterId by rememberSaveable { mutableStateOf<String?>(null) }

    // Новые переменные для улучшения обработки нажатий
    var lastTapTime by remember { mutableStateOf(0L) }
    var isBottomSheetClosing by remember { mutableStateOf(false) }

    // Глобальный слушатель для маркеров
    val mapMarkerTapListener = remember {
        object : MapObjectTapListener {
            override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
                val meterId = mapObject.userData as? String
                Timber.d("WorkMapTab: Маркер нажат, userData: $meterId")

                if (meterId == null) return false

                val meter = meters.find { it.id == meterId }
                if (meter == null) {
                    Timber.d("WorkMapTab: Счетчик с ID $meterId не найден")
                    return false
                }

                Timber.d("WorkMapTab: Метка нажата для счетчика ${meter.number} в точке $point")

                // Проверяем, прошло ли достаточно времени с последнего нажатия и не закрывается ли BottomSheet
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime > MIN_TAP_INTERVAL && !isBottomSheetClosing) {
                    lastTapTime = currentTime

                    // Проверяем, не тот же самый маркер
                    if (selectedMeter?.id != meter.id) {
                        Timber.d("WorkMapTab: Устанавливаем новый выбранный счетчик ${meter.number}")
                        selectedMeter = meter
                        lastSelectedMeterId = meter.id
                    } else {
                        Timber.d("WorkMapTab: Маркер уже выбран, счетчик: ${meter.number}")
                    }
                } else {
                    Timber.d("WorkMapTab: Игнорируем нажатие (интервал: ${currentTime - lastTapTime}ms, isClosing: $isBottomSheetClosing)")
                }

                return true
            }
        }
    }

    Timber.d("WorkMapTab: Composition с ${meters.size} счетчиками, местоположение: $userLocation")

    LaunchedEffect(lastSelectedMeterId) {
        if (lastSelectedMeterId != null && selectedMeter == null && !isBottomSheetClosing) {
            Timber.d("WorkMapTab: Восстановление выбранного счетчика с ID $lastSelectedMeterId")
            selectedMeter = meters.find { it.id == lastSelectedMeterId }
        }
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && userLocation == null) {
            Timber.d("WorkMapTab: Запрос местоположения")
            onRequestLocation()
        }
    }

    LaunchedEffect(mapView, meters, userLocation) {
        if (mapView != null && !isMapInitialized && meters.isNotEmpty()) {
            Timber.d("WorkMapTab: Начальная настройка карты")
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

                Timber.d("WorkMapTab: Устанавливаем камеру карты на $centerLat, $centerLon с зумом $optimalZoom")
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
                Timber.d("WorkMapTab: Карта инициализирована")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Отображение карты
        AndroidView(
            factory = { ctx ->
                Timber.d("WorkMapTab: Создание MapView")
                MapKitFactory.getInstance().onStart()
                MapView(ctx).apply {
                    mapView = this
                    map = this.mapWindow.map
                    Timber.d("WorkMapTab: MapView и Map инициализированы")

                    // Убедимся, что жесты включены
                    this.mapWindow.map.isZoomGesturesEnabled = true
                    this.mapWindow.map.isRotateGesturesEnabled = true
                    this.mapWindow.map.isScrollGesturesEnabled = true

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

                    this.mapWindow.map.addCameraListener { _, cameraPosition, _, finished ->
                        if (finished) {
                            previousZoom = cameraPosition.zoom
                            Timber.d("WorkMapTab: Изменен зум карты на $previousZoom")
                        }
                    }
                }
            },
            update = { view ->
                Timber.d("WorkMapTab: Обновление MapView с ${meters.size} счетчиками")

                // Сохраняем ссылку на map для использования в других местах
                map = view.mapWindow.map

                // Включаем все жесты при обновлении
                view.mapWindow.map.isZoomGesturesEnabled = true
                view.mapWindow.map.isRotateGesturesEnabled = true
                view.mapWindow.map.isScrollGesturesEnabled = true

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

                            // Увеличенный масштаб и z-index для лучшего перехвата нажатий
                            val iconStyle = IconStyle().apply {
                                this.scale = 1.0f  // Увеличенный масштаб
                                this.zIndex = 10.0f // Более высокий z-index
                            }

                            val placemarkObject = view.mapWindow.map.mapObjects.addPlacemark(
                                point,
                                ImageProvider.fromBitmap(bitmap),
                                iconStyle
                            )

                            // Сохраняем ID счетчика в userData
                            placemarkObject.userData = meter.id

                            // Используем один глобальный слушатель вместо создания нового при каждом обновлении
                            placemarkObject.addTapListener(mapMarkerTapListener)

                            Timber.d("WorkMapTab: Добавлен маркер для счетчика ${meter.number} с ID ${meter.id}")
                        }
                    }
                }

                // Добавление маркера местоположения пользователя
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
                    Timber.d("WorkMapTab: Добавлен маркер местоположения пользователя")
                }
            },
            onRelease = {
                Timber.d("WorkMapTab: Освобождение MapView")
                mapView?.onStop()
                MapKitFactory.getInstance().onStop()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (locationPermissionState.status.isGranted) {
            FloatingActionButton(
                onClick = {
                    Timber.d("WorkMapTab: Нажата кнопка 'Мое местоположение'")
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
            Timber.d("WorkMapTab: Показываем BottomSheet для счетчика ${selectedMeter?.number}")
            MapBottomSheet(
                meter = selectedMeter!!,
                onDismiss = {
                    Timber.d("WorkMapTab: BottomSheet закрыт")
                    isBottomSheetClosing = true
                    selectedMeter = null

                    // Добавляем задержку перед возможностью повторного открытия
                    coroutineScope.launch {
                        delay(300) // 300 мс задержка
                        isBottomSheetClosing = false
                        Timber.d("WorkMapTab: Готов к приему новых нажатий")
                    }
                },
                onBuildRoute = {
                    Timber.d("WorkMapTab: Построение маршрута для счетчика ${selectedMeter?.number}")
                    selectedMeter?.let { meter ->
                        buildRoute(context, meter, userLocation, navigatorType)
                        onBuildRoute(meter)
                    }
                },
                onTakeReading = {
                    Timber.d("WorkMapTab: Снятие показаний для счетчика ${selectedMeter?.number}")
                    selectedMeter?.let { meter ->
                        onTakeReading(meter.id)
                    }
                }
            )
        } else {
            Timber.d("WorkMapTab: BottomSheet не показан, selectedMeter равен null")
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

    Timber.d("buildRoute: Построение маршрута от ($sourceLat, $sourceLon) к ($destLat, $destLon) с типом $navigatorType")

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
        Timber.d("buildRoute: Запуск навигатора с URI $uri")
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e("buildRoute: Ошибка при запуске навигатора: ${e.message}")
        // Обработка ошибок (например, если требуемый навигатор не установлен)
        // В этом случае можно открыть системный навигатор как запасной вариант
        if (navigatorType != NavigatorType.SYSTEM_DEFAULT) {
            val defaultUri = Uri.parse("geo:$destLat,$destLon?q=$destLat,$destLon(${meter.address.street})")
            val defaultIntent = Intent(Intent.ACTION_VIEW, defaultUri)
            defaultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                Timber.d("buildRoute: Запуск системного навигатора с URI $defaultUri")
                context.startActivity(defaultIntent)
            } catch (e: Exception) {
                Timber.e("buildRoute: Ошибка при запуске системного навигатора: ${e.message}")
                // Ничего не делаем, если и это не сработало
            }
        }
    }
}