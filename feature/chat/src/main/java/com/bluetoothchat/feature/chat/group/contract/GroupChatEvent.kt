package com.bluetoothchat.feature.chat.group.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface GroupChatEvent : ViewOneTimeEvent {

    object NavigateBack : GroupChatEvent

    object RequestBluetoothPermission : GroupChatEvent

    object RequestWriteStoragePermission : GroupChatEvent

    object RequestEnabledBluetooth : GroupChatEvent

    data class ShowDialog(val params: DialogInputParams) : GroupChatEvent

    data class NavigateToChatInfoScreenScreen(val chatId: String) : GroupChatEvent

    data class NavigateToProfileScreen(val userDeviceAddress: String) : GroupChatEvent

    data class NavigateToViewImageScreen(val chatId: String, val messageId: String) : GroupChatEvent

    object OpenExternalGalleryForImage : GroupChatEvent

    object ScrollToLastMessage : GroupChatEvent

    object ShowTextCopiedSnackbar : GroupChatEvent

    object ShowImageSavedSnackbar : GroupChatEvent

    object RequestInputFieldFocus : GroupChatEvent

}
