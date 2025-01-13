package com.bluetoothchat.core.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbDiProvideModule {

    @Provides
    @Singleton
    fun providesDatabaseManager(
        @ApplicationContext context: Context,
    ): DatabaseManager {
        return RoomDatabaseManager.create(context)
    }

}
