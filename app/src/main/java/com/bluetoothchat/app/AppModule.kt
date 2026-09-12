package com.bluetoothchat.app

import com.bluetoothchat.app.deeplink.DeeplinkManager
import com.bluetoothchat.app.file.ApkExtractorImpl
import com.bluetoothchat.app.notification.ActivityKillerImpl
import com.bluetoothchat.app.notification.NotificationIntentCreatorImpl
import com.bluetoothchat.app.notification.NotificationStringProviderImpl
import com.bluetoothchat.app.session.SessionUserColorProviderImpl
import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import com.bluetoothchat.core.bluetooth.notification.NotificationIntentCreator
import com.bluetoothchat.core.bluetooth.notification.NotificationStringProvider
import com.bluetoothchat.core.domain.AppInfoProvider
import com.bluetoothchat.core.filemanager.ApkExtractor
import com.bluetoothchat.core.session.SessionUserColorProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val appModule = module {
    singleOf(::AppInfoProviderImpl) bind AppInfoProvider::class
    singleOf(::ApkExtractorImpl) bind ApkExtractor::class
    singleOf(::SessionUserColorProviderImpl) bind SessionUserColorProvider::class

    singleOf(::NotificationIntentCreatorImpl) bind NotificationIntentCreator::class
    singleOf(::NotificationStringProviderImpl) bind NotificationStringProvider::class
    singleOf(::ActivityKillerImpl) bind ActivityKiller::class

    singleOf(::DeeplinkManager)
}
