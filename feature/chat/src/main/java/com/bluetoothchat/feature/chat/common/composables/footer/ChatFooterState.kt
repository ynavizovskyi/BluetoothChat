package com.bluetoothchat.feature.chat.common.composables.footer

import com.bluetoothchat.core.ui.model.UiText

internal sealed interface ChatFooterState {

    data class InfoWithButton(
        val infoText: UiText,
        val buttonId: Int,
        val buttonEnabled: Boolean,
        val buttonText: UiText,
    ) : ChatFooterState

    data class Info(val message: UiText) : ChatFooterState

    object InputField : ChatFooterState

}
