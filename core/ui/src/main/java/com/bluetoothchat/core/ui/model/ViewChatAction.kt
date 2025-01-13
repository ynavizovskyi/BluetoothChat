package com.bluetoothchat.core.ui.model

import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.mvi.action.ViewAction
import com.bluetoothchat.core.ui.mvi.action.ViewActionConfirmation
import kotlinx.parcelize.Parcelize

private val deleteActionConfirmation = ViewActionConfirmation.Dialog(
    titleStringRes = R.string.chat_action_delete_confirm_title,
    messageStringRes = R.string.chat_action_delete_confirm_text,
    actionStringRes = R.string.chat_action_private_delete,
)

private val leaveActionConfirmation = ViewActionConfirmation.Dialog(
    titleStringRes = R.string.chat_action_leave_confirm_title,
    messageStringRes = R.string.chat_action_leave_confirm_text,
    actionStringRes = R.string.chat_action_group_leave,
)

@Parcelize
sealed interface ViewChatAction : ViewAction {
    val chatId: String

    sealed interface PrivateChat : ViewChatAction {
        @Parcelize
        data class Disconnect(override val chatId: String) : PrivateChat {
            override val nameStringRes = R.string.chat_action_private_disconnect
            override val confirmation = ViewActionConfirmation.None
        }

        @Parcelize
        data class Delete(override val chatId: String) : PrivateChat {
            override val nameStringRes = R.string.chat_action_private_delete
            override val confirmation = deleteActionConfirmation
        }

        @Parcelize
        data class DeleteAndDisconnect(override val chatId: String) : PrivateChat {
            override val nameStringRes = R.string.chat_action_private_delete_and_disconnect
            override val confirmation = deleteActionConfirmation
        }
    }

    sealed interface GroupChat : ViewChatAction {
        sealed interface Host : GroupChat {
            @Parcelize
            data class DisconnectAll(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_disconnect_all
                override val confirmation = ViewActionConfirmation.None
            }

            @Parcelize
            data class Delete(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_delete
                override val confirmation = deleteActionConfirmation
            }

            @Parcelize
            data class DeleteAndDisconnectAll(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_delete_and_disconnect_all
                override val confirmation = deleteActionConfirmation
            }
        }

        sealed interface Client : GroupChat {
            @Parcelize
            data class Disconnect(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_disconnect
                override val confirmation = ViewActionConfirmation.None
            }

            @Parcelize
            data class LeaveGroup(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_leave
                override val confirmation = leaveActionConfirmation
            }

            @Parcelize
            data class LeaveGroupAndDisconnect(override val chatId: String) : Host {
                override val nameStringRes = R.string.chat_action_group_leave_and_disconnect
                override val confirmation = leaveActionConfirmation
            }
        }
    }
}
