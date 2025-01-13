package com.bluetoothchat.core.prefs.session

interface SessionPrefs {

    fun setAnalyticsUserId(id: String)

    fun getAnalyticsUserId(): String

    fun setTotalSessionCount(count: Int)

    fun getTotalSessionCount(): Int

    fun setUserDeviceAddress(address: String)

    fun getUserDeviceAddress(): String

    fun setRateAppNeverShowAgain()

    fun getRateAppNeverShowAgain(): Boolean

    fun setRateAppLastShownTimestamp(timestamp: Long)

    fun getRateAppLastShownTimestamp(): Long

}
