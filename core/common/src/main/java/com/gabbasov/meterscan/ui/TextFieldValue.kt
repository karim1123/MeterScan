package com.gabbasov.meterscan.ui

import androidx.compose.runtime.Stable

@Stable
data class TextFieldValue(
    val value: String = "",
    val label: Text? = null,
    val isError: Boolean = false,
)
