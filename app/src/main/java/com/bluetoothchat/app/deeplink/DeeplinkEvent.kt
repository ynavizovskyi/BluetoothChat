package com.bluetoothchat.app.deeplink

sealed interface DeeplinkEvent {

    object NavigateToConnectScreen : DeeplinkEvent

    data class NavigateToPrivateChat(val chatId: String) : DeeplinkEvent

    data class NavigateToGroupChat(val chatId: String) : DeeplinkEvent

}
