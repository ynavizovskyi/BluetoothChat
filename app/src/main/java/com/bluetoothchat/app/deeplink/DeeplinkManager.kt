package com.bluetoothchat.app.deeplink

import android.content.Context
import com.bluetoothchat.core.bluetooth.notification.ChatAppDeeplink
import com.bluetoothchat.core.dispatcher.ApplicationScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DeeplinkManager(
    private val context: Context,
    private val applicationScope: ApplicationScope,
) {

    private val _deeplinkEvents: Channel<DeeplinkEvent> = Channel(Channel.BUFFERED)
    val deeplinkEvents = _deeplinkEvents.receiveAsFlow()

    fun onNewDeeplink(deeplink: ChatAppDeeplink) {
        applicationScope.launch {
            when (deeplink) {
                is ChatAppDeeplink.Connect -> {
                    _deeplinkEvents.send(DeeplinkEvent.NavigateToConnectScreen)
                }
                is ChatAppDeeplink.PrivateChat -> {
                    _deeplinkEvents.send(DeeplinkEvent.NavigateToPrivateChat(chatId = deeplink.chatId))
                }
                is ChatAppDeeplink.GroupChat -> {
                    _deeplinkEvents.send(DeeplinkEvent.NavigateToGroupChat(chatId = deeplink.chatId))
                }
            }
        }
    }

}
