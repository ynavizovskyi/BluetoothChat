package com.bluetoothchat.feature.connect.group.create

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureCreateGroupModule = module {
    singleOf(::CreateGroupAnalyticsClient)
    factoryOf(::CreateGroupViewModel)
}
