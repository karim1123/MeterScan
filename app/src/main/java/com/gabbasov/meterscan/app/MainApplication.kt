package com.gabbasov.meterscan.app

import android.app.Application
import com.gabbasov.meterscan.di.authModule
import com.gabbasov.meterscan.di.signUpFeatureMode
import com.gabbasov.meterscan.network.di.coroutineDispatchersModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                coroutineDispatchersModule,
                authModule,
                signUpFeatureMode,
            )
        }
    }
}
