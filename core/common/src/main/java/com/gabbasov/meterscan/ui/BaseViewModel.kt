package com.gabbasov.meterscan.ui

import androidx.lifecycle.ViewModel
import com.gabbasov.meterscan.domain.BaseAction
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {
    open fun execute(action: BaseAction) {
        Timber.d("TEST123 execute action: $action")
    }
}
