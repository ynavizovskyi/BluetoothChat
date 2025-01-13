package com.bluetoothchat.core.bluetooth.notification

import android.app.PendingIntent

interface NotificationIntentCreator {

    fun createDeeplinkIntent(deeplink: ChatAppDeeplink): PendingIntent

}
