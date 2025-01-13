package com.bluetoothchat.core.config

import com.bluetoothchat.core.config.model.AnalyticsConfig
import kotlinx.coroutines.flow.Flow

interface RemoteConfig {

    suspend fun getAnalyticsConfig(): AnalyticsConfig

    fun observeAnalyticsConfig(): Flow<AnalyticsConfig>

    suspend fun getRawConfig(): Map<String, Any>

    fun observeRawConfig(): Flow<Map<String, Any>>

}
