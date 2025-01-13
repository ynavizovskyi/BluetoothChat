package com.bluetoothchat.feature.connect.main.contract

import android.net.Uri
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface ConnectEvent : ViewOneTimeEvent {

    object NavigateBack : ConnectEvent

    object RequestBluetoothPermission : ConnectEvent

    object RequestEnabledBluetooth : ConnectEvent

    object MakeDeviceDiscoverable : ConnectEvent

    object NavigateToCreateGroupScreen : ConnectEvent

    data class ShowErrorDialog(val params: DialogInputParams) : ConnectEvent

    data class NavigateToPrivateChatScreen(val chatId: String) : ConnectEvent

    data class NavigateToGroupChatScreen(val chatId: String) : ConnectEvent

    data class ShareApk(val uri: Uri) : ConnectEvent

}
