package com.gabbasov.meterscan.domain

import com.gabbasov.meterscan.ui.Text

abstract class BaseState(
    open val isLoading: Boolean = false,
    open val error: Text? = null,
)
