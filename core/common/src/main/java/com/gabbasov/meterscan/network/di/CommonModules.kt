package com.gabbasov.meterscan.network.di

import com.gabbasov.meterscan.network.CoroutineDispatchers
import org.koin.dsl.module

val coroutineDispatchersModule =
    module {
        single { CoroutineDispatchers() }
    }
