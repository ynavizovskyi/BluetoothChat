package com.bluetoothchat.feature.chat.privat

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featurePrivateChatModule = module {
    singleOf(::PrivateChatAnalyticsClient)
    factoryOf(::PrivateChatViewModel)
}
