package com.gabbasov.meterscan.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector
)

/*
@Composable
fun MeterScanBottomAppBar(
    items: List<BottomNavItem>,
    onItemClick: (BottomNavItem) -> Unit
) {
    BottomAppBar {
        IconButton(onClick = onGalleryClick) {
            Icon(imageVector = Icons.Default.Call, contentDescription = "Галерея")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onCaptureClick) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Снять")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onSettingsClick) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Настройки")
        }
    }
}*/
