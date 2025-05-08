package com.gabbasov.meterscan.meters.di

import androidx.room.Room
import com.gabbasov.meterscan.features.MetersFeatureApi
import com.gabbasov.meterscan.meters.data.MetersRepositoryImpl
import com.gabbasov.meterscan.meters.data.db.MeterScanDatabase
import com.gabbasov.meterscan.meters.data.db.mock.MockDataProvider
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.meters.presentation.details.MeterDetailViewModel
import com.gabbasov.meterscan.meters.presentation.list.MetersListViewModel
import com.gabbasov.meterscan.meters.presentation.navigation.MetersNavigation
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val metersFeatureModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MeterScanDatabase::class.java,
            MeterScanDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<MeterScanDatabase>().meterDao() }
    single { get<MeterScanDatabase>().readingDao() }
    single { MockDataProvider(get()) }

    singleOf(::MetersRepositoryImpl) bind MetersRepository::class
    singleOf(::MetersNavigation) bind MetersFeatureApi::class
    viewModel { MetersListViewModel(get()) }
    viewModel { MeterDetailViewModel(get()) }
}
