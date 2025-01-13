package com.bluetoothchat.feature.connect.group.create.contract

import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface CreateGroupEvent : ViewOneTimeEvent {

    object NavigateBack : CreateGroupEvent

    object OpenExternalGalleryForImage : CreateGroupEvent

    data class NavigateToGroupInfo(val groupId: String) : CreateGroupEvent

    data class ShowErrorDialog(val params: DialogInputParams) : CreateGroupEvent

}
