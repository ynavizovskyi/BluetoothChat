package com.bluetoothchat.core.analytics

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreAnalyticsModule = module {
    singleOf(::AmplitudeAnalyticsClient) bind AnalyticsClient::class
}
