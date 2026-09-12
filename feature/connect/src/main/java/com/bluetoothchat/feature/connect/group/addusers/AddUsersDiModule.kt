package com.bluetoothchat.feature.connect.group.addusers

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureAddUsersModule = module {
    singleOf(::AddUsersAnalyticsClient)
    factoryOf(::AddUserViewModel)
}
