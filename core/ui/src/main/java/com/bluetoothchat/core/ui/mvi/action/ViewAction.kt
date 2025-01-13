package com.bluetoothchat.core.ui.mvi.action

import android.os.Parcelable
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewChatAction

interface ViewAction : Parcelable {
    val nameStringRes: Int
    val confirmation: ViewActionConfirmation
}

fun ViewChatAction.toDropdownMenuItem() = DropdownMenuItemModel(
    text = UiText.Resource(this.nameStringRes),
    data = this,
)
