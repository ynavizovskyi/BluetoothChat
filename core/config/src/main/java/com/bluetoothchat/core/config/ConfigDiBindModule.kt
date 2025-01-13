package com.bluetoothchat.core.config

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ConfigDiBindModule {

    @Singleton
    @Binds
    abstract fun bindRemoteConfig(config: FirebaseRemoteConfig): RemoteConfig

}
