package com.bluetoothchat.core.prefs.session

import android.content.Context
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.prefs.USER_DEVICE_ADDRESS_NOT_SET
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionPrefsImpl @Inject constructor(
    @ApplicationContext context: Context,
    dispatcherManager: DispatcherManager,
) : SessionPrefs {

    private val preferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private val flowPreferences = FlowSharedPreferences(preferences, dispatcherManager.io)

    override fun setAnalyticsUserId(id: String) =
        flowPreferences.getString(KEY_USER_ID, USER_ID_DEFAULT).set(id)

    override fun getAnalyticsUserId(): String =
        flowPreferences.getString(KEY_USER_ID, USER_ID_DEFAULT).get()

    override fun setTotalSessionCount(count: Int) =
        flowPreferences.getInt(KEY_TOTAL_SESSION_COUNT, TOTAL_SESSION_COUNT_DEFAULT).set(count)

    override fun getTotalSessionCount(): Int =
        flowPreferences.getInt(KEY_TOTAL_SESSION_COUNT, TOTAL_SESSION_COUNT_DEFAULT).get()

    override fun setUserDeviceAddress(address: String) =
        flowPreferences.getString(KEY_USER_DEVICE_ADDRESS, USER_DEVICE_ADDRESS_NOT_SET).set(address)

    override fun getUserDeviceAddress(): String =
        flowPreferences.getString(KEY_USER_DEVICE_ADDRESS, USER_DEVICE_ADDRESS_NOT_SET).get()

    override fun setRateAppNeverShowAgain() =
        flowPreferences.getBoolean(KEY_RATE_APP_NEVER_SHOW_AGAIN, RATE_APP_NEVER_SHOW_AGAIN_DEFAULT).set(true)

    override fun getRateAppNeverShowAgain(): Boolean =
        flowPreferences.getBoolean(KEY_RATE_APP_NEVER_SHOW_AGAIN, RATE_APP_NEVER_SHOW_AGAIN_DEFAULT).get()

    override fun setRateAppLastShownTimestamp(timestamp: Long) =
        flowPreferences.getLong(KEY_RATE_APP_LAST_SHOWN_TIMESTAMP, RATE_APP_LAST_SHOWN_TIMESTAMP_DEFAULT).set(timestamp)

    override fun getRateAppLastShownTimestamp(): Long =
        flowPreferences.getLong(KEY_RATE_APP_LAST_SHOWN_TIMESTAMP, RATE_APP_LAST_SHOWN_TIMESTAMP_DEFAULT).get()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val USER_ID_DEFAULT = ""
        private const val KEY_TOTAL_SESSION_COUNT = "total_session_count"
        private const val TOTAL_SESSION_COUNT_DEFAULT = 0
        private const val KEY_USER_DEVICE_ADDRESS = "user_device_address"
        private const val KEY_RATE_APP_NEVER_SHOW_AGAIN = "rate_app_never_show_again"
        private const val RATE_APP_NEVER_SHOW_AGAIN_DEFAULT = false
        private const val KEY_RATE_APP_LAST_SHOWN_TIMESTAMP = "rate_app_last_shown_timestamp"
        private const val RATE_APP_LAST_SHOWN_TIMESTAMP_DEFAULT = 0L
    }
}
