package com.bluetoothchat.feature.settings

import com.bluetoothchat.feature.settings.ui.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureSettingsModule = module {
    singleOf(::SettingsAnalyticsClient)
    factoryOf(::SettingsViewModel)
}
