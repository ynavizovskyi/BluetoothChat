package com.bluetoothchat.core.prefs

import com.bluetoothchat.core.prefs.billing.BillingPrefs
import com.bluetoothchat.core.prefs.billing.BillingPrefsImp
import com.bluetoothchat.core.prefs.session.SessionPrefs
import com.bluetoothchat.core.prefs.session.SessionPrefsImpl
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefsImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val corePrefsModule = module {
    singleOf(::SessionPrefsImpl) bind SessionPrefs::class
    singleOf(::BillingPrefsImp) bind BillingPrefs::class
    singleOf(::AppSettingsPrefsImpl) bind AppSettingsPrefs::class
}
