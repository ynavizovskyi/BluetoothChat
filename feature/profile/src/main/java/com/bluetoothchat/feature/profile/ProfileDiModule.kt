package com.bluetoothchat.feature.profile

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureProfileModule = module {
    singleOf(::ProfileAnalyticsClient)
    factoryOf(::ProfileViewModel)
}
