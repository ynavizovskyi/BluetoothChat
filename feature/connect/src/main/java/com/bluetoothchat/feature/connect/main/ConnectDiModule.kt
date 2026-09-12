package com.bluetoothchat.feature.connect.main

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureConnectModule = module {
    singleOf(::ConnectAnalyticsClient)
    factoryOf(::ConnectViewModel)
}
