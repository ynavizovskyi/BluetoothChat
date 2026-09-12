package com.bluetoothchat.core.bluetooth

import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.ConnectionDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.FileDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.GroupChatDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.NotificationDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.PrivateChatDelegateImpl
import com.bluetoothchat.core.bluetooth.notification.NotificationManagerWrapper
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreBluetoothModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    singleOf(::BtScanner)
    singleOf(::BtServiceManager)
    singleOf(::BtConnectionManager)
    singleOf(::MessageManager)
    singleOf(::NotificationManagerWrapper)

    singleOf(::ConnectionDelegateImpl)
    singleOf(::FileDelegateImpl)
    singleOf(::GroupChatDelegateImpl)
    singleOf(::NotificationDelegateImpl)
    singleOf(::PrivateChatDelegateImpl)
    singleOf(::CommunicationManagerImpl)
}
