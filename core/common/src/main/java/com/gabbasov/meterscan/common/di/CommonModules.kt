package com.gabbasov.meterscan.common.di

import com.gabbasov.meterscan.common.network.CoroutineDispatchers
import org.koin.dsl.module

val coroutineDispatchersModule =
    module {
        single { CoroutineDispatchers() }
    }
