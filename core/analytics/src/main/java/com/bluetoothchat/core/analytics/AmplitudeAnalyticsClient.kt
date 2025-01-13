package com.bluetoothchat.core.analytics

import android.app.Application
import android.content.Context
import android.util.Log
import com.amplitude.api.Amplitude
import com.bluetoothchat.core.config.RemoteConfig
import com.bluetoothchat.core.dispatcher.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmplitudeAnalyticsClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: RemoteConfig,
    private val applicationScope: ApplicationScope,
) : AnalyticsClient {

    private val client = Amplitude.getInstance().apply {
        initialize(context, BuildConfig.AMPLITUDE_API_KEY)
        trackSessionEvents(true)
        enableForegroundTracking(context as Application)
        setOffline(true) //Do not upload any events until the remote config is fetched
    }

    init {
        remoteConfig
            .observeAnalyticsConfig()
            .onEach { analyticsConfig ->
                client.setOffline(!analyticsConfig.analyticsEnabled)
            }
            .launchIn(applicationScope)
    }

    override suspend fun setUserId(userId: String) {
        client.userId = userId
    }

    override suspend fun clearUserProperties() {
        client.clearUserProperties()
    }

    override suspend fun setUserProperty(name: String, value: Any) {
        client.setUserProperties(JSONObject(mapOf(name to value)))
    }

    override suspend fun setUserProperties(properties: Map<String, Any>) {
        client.setUserProperties(JSONObject(properties))
    }

    override suspend fun logEvent(name: String, params: Map<String, Any>) {
        client.logEvent(name, JSONObject(params))
        if (BuildConfig.DEBUG) {
            Log.v("debugAnalytics", "$name $params")
        }
    }

}
