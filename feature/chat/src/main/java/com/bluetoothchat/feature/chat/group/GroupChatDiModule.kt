package com.bluetoothchat.feature.chat.group

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureGroupChatModule = module {
    singleOf(::GroupChatAnalyticsClient)
    factoryOf(::GroupChatViewModel)
}
