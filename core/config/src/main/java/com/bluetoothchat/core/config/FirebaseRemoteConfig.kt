package com.bluetoothchat.core.config

import android.util.Log
import com.bluetoothchat.core.config.model.AnalyticsConfig
import com.bluetoothchat.core.config.model.LegalsConfig
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
    private val legalsConfigFlow = MutableStateFlow(defaultLegalsConfig)
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
        val legalsConfig = getLegalsConfig()
        val rawConfig = getRawConfig()
        analyticsConfigFlow.value = analyticsConfig
        legalsConfigFlow.value = legalsConfig
        rawConfigFlow.value = rawConfig
        Log.v("FirebaseRemoteConfig", rawConfig.toString())
    }

    override suspend fun getAnalyticsConfig(): AnalyticsConfig {
        val analyticsEnabled = config.getBoolean(KEY_ANALYTICS_ENABLED)
        return AnalyticsConfig(analyticsEnabled = analyticsEnabled)
    }

    override fun observeAnalyticsConfig() = analyticsConfigFlow

    override suspend fun getLegalsConfig(): LegalsConfig {
        val termsOfUseUrl = config.getString(KEY_TERMS_OF_USE_URL)
        val privacyPolicyUrl = config.getString(KEY_PRIVACY_POLICY_URL)
        return LegalsConfig(
            termsOfUseUrl = termsOfUseUrl,
            privacyPolicyUrl = privacyPolicyUrl,
        )
    }

    override fun observeLegalsConfig() = legalsConfigFlow

    override fun observeRawConfig() = rawConfigFlow

    //TODO: !!!! NEEDS TO BE UPDATED WITH EVERY NEW VALUE !!!!
    override suspend fun getRawConfig(): Map<String, Any> {
        val rawConfig = mutableMapOf<String, Any>()
        rawConfig[KEY_ANALYTICS_ENABLED] = config.getBoolean(KEY_ANALYTICS_ENABLED)
        rawConfig[KEY_TERMS_OF_USE_URL] = config.getBoolean(KEY_TERMS_OF_USE_URL)
        rawConfig[KEY_PRIVACY_POLICY_URL] = config.getBoolean(KEY_PRIVACY_POLICY_URL)
        return rawConfig
    }

    companion object {
        //Let's fetch the config on every launch for now
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 0L

        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        private const val KEY_TERMS_OF_USE_URL = "terms_of_use_url"
        private const val KEY_PRIVACY_POLICY_URL = "privacy_policy_url"

        private val defaultAnalyticsConfig = AnalyticsConfig(analyticsEnabled = false)

        private val defaultLegalsConfig = LegalsConfig(
            termsOfUseUrl = "https://doc-hosting.flycricket.io/bluetooth-chat-terms-of-use/4f4d7f55-1e53-4257-81a3-aad2cbb0e044/terms",
            privacyPolicyUrl = "https://doc-hosting.flycricket.io/bluetooth-chat-privacy-policy/bfce3de7-0c38-4098-afbd-95f2accf9b83/privacy",
        )

        private val defaults: Map<String, Any> = mapOf(
            KEY_ANALYTICS_ENABLED to defaultAnalyticsConfig.analyticsEnabled,
            KEY_TERMS_OF_USE_URL to defaultLegalsConfig.termsOfUseUrl,
            KEY_PRIVACY_POLICY_URL to defaultLegalsConfig.privacyPolicyUrl,
        )
    }
}
