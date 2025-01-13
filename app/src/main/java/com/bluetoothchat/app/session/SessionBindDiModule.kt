package com.bluetoothchat.app.session

import com.bluetoothchat.core.session.SessionUserColorProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SessionBindDiModule {

    @Singleton
    @Binds
    abstract fun bindUserColorProvider(provider: SessionUserColorProviderImpl): SessionUserColorProvider

}
