package com.bluetoothchat.feature.main

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureMainModule = module {
    singleOf(::MainAnalyticsClient)
    factoryOf(::MainViewModel)
}
