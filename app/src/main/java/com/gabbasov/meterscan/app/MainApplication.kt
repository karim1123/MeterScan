package com.gabbasov.meterscan.app

import android.app.Application
import com.gabbasov.meterscan.auth.di.signUpFeatureMode
import com.gabbasov.meterscan.di.authModule
import com.gabbasov.meterscan.di.coroutineDispatchersModule
import com.gabbasov.meterscan.di.mainModule
import com.gabbasov.meterscan.main.di.mainFeatureModule
import com.gabbasov.meterscan.meters.di.metersFeatureModule
import com.gabbasov.meterscan.scan.di.scanModule
import com.gabbasov.meterscan.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                mainModule,
                coroutineDispatchersModule,
                authModule,
                signUpFeatureMode,
                mainFeatureModule,
                metersFeatureModule,
                settingsModule,
                scanModule,
            )
        }
    }
}
