package com.bluetoothchat.core.config

import android.util.Log
import com.bluetoothchat.core.config.model.AnalyticsConfig
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfig @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
) : RemoteConfig {

    private val analyticsConfigFlow = MutableStateFlow(defaultAnalyticsConfig)
    private val rawConfigFlow = MutableStateFlow(emptyMap<String, Any>())

    private val config = FirebaseRemoteConfig.getInstance()
    private val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
        .build()

    init {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.v("FirebaseRemoteConfig", "Failed to load remote config: $exception")
        }
        applicationScope.launch(dispatcherManager.io + handler) {
            fetchAndActivateConfigParameters()
        }
    }

    private suspend fun fetchAndActivateConfigParameters() {
        config.setConfigSettingsAsync(configSettings).await()
        config.setDefaultsAsync(defaults).await()
        config.fetchAndActivate().await()
        parseConfig()
    }

    private suspend fun parseConfig() {
        val analyticsConfig = getAnalyticsConfig()
        val rawConfig = getRawConfig()
        analyticsConfigFlow.value = analyticsConfig
        rawConfigFlow.value = rawConfig
        Log.v("FirebaseRemoteConfig", rawConfig.toString())
    }

    override fun observeAnalyticsConfig() = analyticsConfigFlow

    override fun observeRawConfig() = rawConfigFlow

    override suspend fun getAnalyticsConfig(): AnalyticsConfig {
        val analyticsEnabled = config.getBoolean(KEY_ANALYTICS_ENABLED)
        return AnalyticsConfig(analyticsEnabled = analyticsEnabled)
    }

    //TODO: !!!! NEEDS TO BE UPDATED WITH EVERY NEW VALUE !!!!
    override suspend fun getRawConfig(): Map<String, Any> {
        val rawConfig = mutableMapOf<String, Any>()
        rawConfig[KEY_ANALYTICS_ENABLED] = config.getBoolean(KEY_ANALYTICS_ENABLED)
        return rawConfig
    }

    companion object {
        //Let's fetch the config on every launch for now
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 0L

        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"

        private val defaultAnalyticsConfig = AnalyticsConfig(analyticsEnabled = false)

        private val defaults: Map<String, Any> = mapOf(
            KEY_ANALYTICS_ENABLED to defaultAnalyticsConfig.analyticsEnabled,
        )
    }
}
