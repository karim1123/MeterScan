package com.gabbasov.meterscan.ui.components.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.gabbasov.meterscan.designsystem.MeterScanTheme

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    companion object {
        fun getBottomNavItems(): List<BottomNavItem> {
            return listOf(
                BottomNavItem(
                    route = "home",
                    icon = Icons.Default.Home,
                    label = "Home"
                ),
                BottomNavItem(
                    route = "meters",
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Meters"
                ),
                BottomNavItem(
                    route = "settings",
                    icon = Icons.Default.Settings,
                    label = "Settings"
                )
            )
        }
    }
}

@Composable
fun MeterScanBottomAppBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
                selected = currentRoute == item.route,
                onClick = { onItemClick(item) },
                alwaysShowLabel = true
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MeterScanBottomAppBarPreview() {
    val bottomNavItems = listOf(
        BottomNavItem(
            route = "home",
            icon = Icons.Default.Home,
            label = "Home"
        ),
        BottomNavItem(
            route = "meters",
            icon = Icons.AutoMirrored.Filled.List,
            label = "Meters"
        ),
        BottomNavItem(
            route = "settings",
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )

    MeterScanTheme {
        MeterScanBottomAppBar(
            items = bottomNavItems,
            currentRoute = "home",
            onItemClick = {}
        )
    }
}