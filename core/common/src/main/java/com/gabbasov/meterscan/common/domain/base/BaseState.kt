package com.gabbasov.meterscan.common.domain.base

import com.gabbasov.meterscan.common.ui.Text

abstract class BaseState(
    open val isLoading: Boolean = false,
    open val error: Text? = null,
)
