package com.bluetoothchat.core.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AnalyticsDiBindModule {

    @Singleton
    @Binds
    abstract fun bindAnalytics(amplitudeClient: AmplitudeAnalyticsClient): AnalyticsClient

}
