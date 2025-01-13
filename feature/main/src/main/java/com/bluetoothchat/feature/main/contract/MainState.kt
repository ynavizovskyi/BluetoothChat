package com.bluetoothchat.feature.main.contract

import androidx.compose.runtime.Composable
import com.bluetoothchat.core.ui.model.ViewChatWithMessages
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal sealed interface MainState : ViewState {

    object Loading : MainState

    data class Loaded(
        val user: ViewUser,
        val chats: List<ViewChatWithMessages>,
    ) : MainState

}


