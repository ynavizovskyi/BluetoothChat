package com.bluetoothchat.feature.chat.group.chatinfo.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface GroupChatInfoEvent : ViewOneTimeEvent {

    object NavigateBack : GroupChatInfoEvent

    data class NavigateToAddUsersScreen(val chatId: String) : GroupChatInfoEvent

    data class NavigateToProfileScreen(val userDeviceAddress: String) : GroupChatInfoEvent

    data class ShowDialog(val params: DialogInputParams) : GroupChatInfoEvent

    object OpenExternalGalleryForImage : GroupChatInfoEvent

}
