package com.gabbasov.meterscan.ui.modifiers

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

//todo переписать на Modifier Node
fun Modifier.hideKeyboardOnTap(): Modifier = composed {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    this.pointerInput(Unit) {
        detectTapGestures {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
}