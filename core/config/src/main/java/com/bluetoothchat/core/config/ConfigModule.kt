package com.bluetoothchat.core.config

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreConfigModule = module {
    singleOf(::FirebaseRemoteConfig) bind RemoteConfig::class
}
