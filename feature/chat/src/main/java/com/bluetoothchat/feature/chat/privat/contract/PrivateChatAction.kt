package com.bluetoothchat.feature.chat.privat.contract

import android.net.Uri
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.model.ViewChatAction
import com.bluetoothchat.core.ui.model.ViewMessage
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.mvi.contract.ViewAction
import com.bluetoothchat.feature.chat.common.composables.footer.ChatFooterAction

internal sealed interface PrivateChatAction : ViewAction {

    object BackButtonClicked : PrivateChatAction

    data class OnResumedStateChanged(val isResumed: Boolean) : PrivateChatAction

    data class OnDialogResult(val result: DialogResult) : PrivateChatAction

    data class OnFirstVisibleItemChanged(val itemId: String?) : PrivateChatAction

    data class UserClicked(val user: ViewUser) : PrivateChatAction

    data class MessageImageClicked(val message: ViewMessage) : PrivateChatAction

    data class MessageActionClicked(val action: ViewMessageAction, val message: ViewMessage.Plain) : PrivateChatAction

    data class ChatActionClicked(val action: ViewChatAction) : PrivateChatAction

    data class FooterActionClicked(val action: ChatFooterAction) : PrivateChatAction

    data class ExternalGalleryContentSelected(val uri: Uri) : PrivateChatAction

    data class OnBluetoothPermissionResult(val granted: Boolean) : PrivateChatAction

    data class OnWriteStoragePermissionResult(val granted: Boolean) : PrivateChatAction

    data class OnEnableBluetoothResult(val enabled: Boolean) : PrivateChatAction

}
