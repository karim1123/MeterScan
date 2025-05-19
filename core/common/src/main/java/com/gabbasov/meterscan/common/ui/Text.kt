@file:Suppress("UNSUPPORTED_FEATURE")

package com.gabbasov.meterscan.common.ui

import android.content.Context
import androidx.annotation.StringRes

sealed class Text {
    data class RawString(val value: String) : Text()

    data class ResourceString(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : Text()

    context (Context)
    fun asText(): String =
        when (this) {
            is RawString -> value
            is ResourceString -> getString(resId, *args.toTypedArray())
        }
}
