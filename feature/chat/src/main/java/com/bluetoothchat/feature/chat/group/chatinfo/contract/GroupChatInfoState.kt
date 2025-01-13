package com.bluetoothchat.feature.chat.group.chatinfo.contract

import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal sealed interface GroupChatInfoState : ViewState {

    object Loading : GroupChatInfoState

    sealed interface Loaded : GroupChatInfoState {
        val chat: ViewChat.Group

        fun copyState(chat: ViewChat.Group): Loaded

        data class Host(
            override val chat: ViewChat.Group,
            val editMode: EditMode,
        ) : Loaded {
            override fun copyState(chat: ViewChat.Group) = copy(chat = chat)
        }

        data class Client(override val chat: ViewChat.Group) : Loaded {
            override fun copyState(chat: ViewChat.Group) = copy(chat = chat)
        }
    }
}

internal sealed interface EditMode {
    object None : EditMode

    data class Editing(
        val updatedPictureFileState: FileState?,
        val updatedName: String?,
        val displayEmptyUpdatedNameError: Boolean,
    ) : EditMode
}
