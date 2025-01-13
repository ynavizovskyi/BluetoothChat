package com.bluetoothchat.feature.connect.group.create.contract

import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal data class CreateGroupState(
    val groupImageChatState: FileState?,
    val chatName: String,
    val displayNameError: Boolean,
) : ViewState
