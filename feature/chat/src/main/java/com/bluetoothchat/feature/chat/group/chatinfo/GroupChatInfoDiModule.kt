package com.bluetoothchat.feature.chat.group.chatinfo

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureGroupChatInfoModule = module {
    singleOf(::GroupChatInfoAnalyticsClient)
    factoryOf(::GroupChatInfoViewModel)
}
