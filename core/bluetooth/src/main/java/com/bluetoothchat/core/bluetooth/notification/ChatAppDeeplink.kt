package com.bluetoothchat.core.bluetooth.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ChatAppDeeplink : Parcelable {

    @Parcelize
    object Connect : ChatAppDeeplink

    @Parcelize
    data class PrivateChat(val chatId: String) : ChatAppDeeplink

    @Parcelize
    data class GroupChat(val chatId: String) : ChatAppDeeplink

}
