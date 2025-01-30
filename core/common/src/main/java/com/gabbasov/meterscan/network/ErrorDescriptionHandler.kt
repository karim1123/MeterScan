package com.gabbasov.meterscan.network

import androidx.annotation.StringRes
import com.gabbasov.meterscan.R
import com.gabbasov.meterscan.ui.Text

enum class Errors(@StringRes val errorMessageRes: Int) {
    EMPTY_EMAIL(R.string.error_empty_email),
    INVALID_EMAIL(R.string.error_invalid_email),
    EMPTY_PASSWORD(R.string.error_empty_password),
    SHORT_PASSWORD(R.string.error_short_password),
    PASSWORDS_DO_NOT_MATCH(R.string.error_passwords_do_not_match),
    UNKNOWN(R.string.error_unknown);

    companion object {
        fun getErrorFromName(name: String): Errors {
            return entries.find { it.name == name } ?: UNKNOWN
        }

        fun getErrorText(error: Errors): Text {
            return Text.ResourceString(error.errorMessageRes)
        }
    }
}
