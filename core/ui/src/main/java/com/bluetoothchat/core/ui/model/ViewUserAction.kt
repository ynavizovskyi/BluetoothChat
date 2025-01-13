package com.bluetoothchat.core.ui.model

import androidx.annotation.StringRes
import com.bluetoothchat.core.ui.R

enum class ViewUserAction(@StringRes val nameStringRes: Int) {
    REMOVE_FROM_GROUP(R.string.user_action_remove_from_group),
    REMOVE_FROM_GROUP_AND_DISCONNECT(R.string.user_action_remove_from_group_and_disconnect),
}

