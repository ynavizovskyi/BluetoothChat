package com.bluetoothchat.core.prefs

import com.bluetoothchat.core.prefs.billing.BillingPrefs
import com.bluetoothchat.core.prefs.billing.BillingPrefsImp
import com.bluetoothchat.core.prefs.session.SessionPrefs
import com.bluetoothchat.core.prefs.session.SessionPrefsImpl
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class PrefsDiBindModule {

    @Singleton
    @Binds
    abstract fun bindSessionPrefs(sessionPrefs: SessionPrefsImpl): SessionPrefs

    @Singleton
    @Binds
    abstract fun bindBillingPrefs(billingPrefs: BillingPrefsImp): BillingPrefs

    @Singleton
    @Binds
    abstract fun bindSettingsPrefs(settingsPrefs: AppSettingsPrefsImpl): AppSettingsPrefs

}
