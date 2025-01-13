package com.bluetoothchat.core.ui.model

import androidx.annotation.StringRes
import com.bluetoothchat.core.ui.R

enum class ViewMessageAction(@StringRes val nameStringRes: Int) {
    REPLY(R.string.message_action_reply),
    COPY(R.string.message_action_copy),
    SAVE_TO_GALLERY(R.string.message_action_save_to_gallery),
}
