package com.bluetoothchat.app.notification

import android.app.PendingIntent
import android.content.Context
import com.bluetoothchat.app.MainActivity
import com.bluetoothchat.core.bluetooth.notification.ChatAppDeeplink
import com.bluetoothchat.core.bluetooth.notification.NotificationIntentCreator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationIntentCreatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationIntentCreator {

    val random = Random()

    override fun createDeeplinkIntent(deeplink: ChatAppDeeplink): PendingIntent {
        val intent = MainActivity.createIntent(context = context, inputParams = deeplink)
        return PendingIntent.getActivity(context, random.nextInt(), intent, PendingIntent.FLAG_IMMUTABLE)
    }

}
