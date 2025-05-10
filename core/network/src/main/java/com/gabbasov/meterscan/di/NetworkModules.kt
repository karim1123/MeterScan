package com.gabbasov.meterscan.di

import com.gabbasov.meterscan.auth.AuthDataSource
import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val authModule =
    module {
        single { FirebaseAuth.getInstance() }
        single { AuthDataSource(get(), get()) }
    }
