package com.bluetoothchat.feature.chat.privat.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface PrivateChatEvent : ViewOneTimeEvent {

    object NavigateBack : PrivateChatEvent

    object RequestBluetoothPermission : PrivateChatEvent

    object RequestWriteStoragePermission : PrivateChatEvent

    object RequestEnabledBluetooth : PrivateChatEvent

    data class ShowDialog(val params: DialogInputParams) : PrivateChatEvent

    object OpenExternalGalleryForImage : PrivateChatEvent

    object ScrollToLastMessage : PrivateChatEvent

    object RequestInputFieldFocus : PrivateChatEvent

    object ShowImageSavedSnackbar : PrivateChatEvent

    object ShowTextCopiedSnackbar : PrivateChatEvent

    data class NavigateToViewImageScreen(val chatId: String, val messageId: String) : PrivateChatEvent

    data class NavigateToProfileScreen(val userDeviceAddress: String) : PrivateChatEvent

}
