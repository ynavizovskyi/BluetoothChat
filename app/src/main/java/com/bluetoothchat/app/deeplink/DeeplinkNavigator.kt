package com.bluetoothchat.app.deeplink

interface DeeplinkNavigator {

    suspend fun navigateToConnectScreen()

    suspend fun navigateToPrivateChat(chatId: String)

    suspend fun navigateToGroupChat(chatId: String)

}
