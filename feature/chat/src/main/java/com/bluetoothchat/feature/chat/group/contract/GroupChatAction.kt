package com.bluetoothchat.feature.chat.group.contract

import android.net.Uri
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.model.ViewChatAction
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.mvi.contract.ViewAction
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterAction

internal sealed interface GroupChatAction : ViewAction {

    object BackButtonClicked : GroupChatAction

    data class OnResumedStateChanged(val isResumed: Boolean) : GroupChatAction

    object ChatImageClicked : GroupChatAction

    data class OnDialogResult(val result: DialogResult) : GroupChatAction

    data class OnFirstVisibleItemChanged(val itemId: String?) : GroupChatAction

    data class FooterActionClicked(val action: ChatFooterAction) : GroupChatAction

    data class UserClicked(val user: ViewUser) : GroupChatAction

    data class MessageImageClicked(val message: ViewMessage) : GroupChatAction

    data class MessageActionClicked(val action: ViewMessageAction, val message: ViewMessage.Plain) : GroupChatAction

    data class ChatActionClicked(val action: ViewChatAction) : GroupChatAction

    data class ExternalGalleryContentSelected(val uri: Uri) : GroupChatAction

    data class OnBluetoothPermissionResult(val granted: Boolean) : GroupChatAction

    data class OnWriteStoragePermissionResult(val granted: Boolean) : GroupChatAction

    data class OnEnableBluetoothResult(val enabled: Boolean) : GroupChatAction

}
