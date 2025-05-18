package com.gabbasov.meterscan.ui

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.domain.base.BaseAction
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {
    fun logAction(action: BaseAction) {
        Timber.d("TEST123 execute action: $action")
    }
}
