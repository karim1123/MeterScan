package com.gabbasov.meterscan.work.presentation.list.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gabbasov.meterscan.work.presentation.list.WorkState
import com.gabbasov.meterscan.work.presentation.list.pager.list.WorkMetersList
import com.gabbasov.meterscan.work.presentation.list.pager.map.WorkMapTab
import kotlinx.coroutines.launch

@Composable
internal fun WorkScreenViewPager(
    metersState: WorkState,
    onMeterClick: (String) -> Unit,
    onTakeReading: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onRequestLocation: () -> Unit,
    onBuildRoute: (com.gabbasov.meterscan.model.meter.Meter) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = metersState.selectedTabIndex)
    val coroutineScope = rememberCoroutineScope()

    // Синхронизация текущей страницы с выбранным табом
    LaunchedEffect(metersState.selectedTabIndex) {
        if (pagerState.currentPage != metersState.selectedTabIndex) {
            pagerState.animateScrollToPage(metersState.selectedTabIndex)
        }
    }

    // Уведомление о смене вкладки
    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = { Text("Список счетчиков") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = { Text("Карта") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    WorkMetersList(
                        state = metersState,
                        onMeterClick = onMeterClick,
                        onTakeReading = onTakeReading
                    )
                }
                1 -> {
                    WorkMapTab(
                        meters = metersState.meters,
                        userLocation = metersState.userLocation,
                        navigatorType = metersState.navigatorType,
                        onRequestLocation = onRequestLocation,
                        onBuildRoute = onBuildRoute,
                        onTakeReading = onTakeReading,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
