package com.bluetoothchat.feature.chat.image

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureViewImageModule = module {
    singleOf(::ViewImageAnalyticsClient)
    factoryOf(::ViewImageViewModel)
}
