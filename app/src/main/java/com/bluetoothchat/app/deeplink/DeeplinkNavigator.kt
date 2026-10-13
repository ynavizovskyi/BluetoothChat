package com.bluetoothchat.app.deeplink

interface DeeplinkNavigator {

    fun navigateToConnectScreen()

    fun navigateToPrivateChat(chatId: String)

    fun navigateToGroupChat(chatId: String)

}
