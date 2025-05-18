package com.gabbasov.meterscan.main.presentation.tabs

import com.gabbasov.meterscan.NavigationRoute
import com.gabbasov.meterscan.main.R

enum class MainTab(val icon: Int, val titleResId: Int, val route: String) {
    CAMERA(R.drawable.ic_camera, R.string.tab_camera, NavigationRoute.METER_SCAN.route),
    WORK(R.drawable.ic_work, R.string.tab_work, NavigationRoute.WORK.route),
    METERS(R.drawable.ic_meter, R.string.tab_meters, NavigationRoute.METERS_LIST.route),
    SETTINGS(R.drawable.ic_settings, R.string.tab_settings, NavigationRoute.SETTINGS.route)
}
