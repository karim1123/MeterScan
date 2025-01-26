package com.gabbasov.meterscan.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TintTheme(
    val iconTint: Color = Color.Unspecified,
)


val LocalTintTheme = staticCompositionLocalOf { TintTheme() }
