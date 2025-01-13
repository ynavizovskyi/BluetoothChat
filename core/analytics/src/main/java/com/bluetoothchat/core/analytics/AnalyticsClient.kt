package com.bluetoothchat.core.analytics

interface AnalyticsClient {

    suspend fun setUserId(userId: String)

    suspend fun clearUserProperties()

    suspend fun setUserProperty(name: String, value: Any)

    suspend fun setUserProperties(properties: Map<String, Any>)

    suspend fun logEvent(event: AnalyticsEvent) = logEvent(event.name, event.params)

    suspend fun logEvent(name: String) = logEvent(name, emptyMap())

    suspend fun logEvent(name: String, params: Map<String, Any>)

}
