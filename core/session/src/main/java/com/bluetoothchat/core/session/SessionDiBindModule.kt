package com.bluetoothchat.core.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SessionDiBindModule {

    @Singleton
    @Binds
    abstract fun bindSession(sessionImpl: SessionImpl): Session

}
