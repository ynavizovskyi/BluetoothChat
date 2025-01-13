package com.bluetoothchat.app.notification

import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import com.bluetoothchat.core.bluetooth.notification.NotificationIntentCreator
import com.bluetoothchat.core.bluetooth.notification.NotificationStringProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class NotificationDiModule {

    @Singleton
    @Binds
    abstract fun bindIntentCreator(creator: NotificationIntentCreatorImpl): NotificationIntentCreator

    @Singleton
    @Binds
    abstract fun bindStringProvider(provider: NotificationStringProviderImpl): NotificationStringProvider

    @Singleton
    @Binds
    abstract fun bindActivityKiller(killer: ActivityKillerImpl): ActivityKiller

}
