package com.bluetoothchat.core.ui.components.dialog.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DialogResult(
    val dialogId: Int,
    val actionPayload: Parcelable?,
    //null if dismissed without selecting any option
    val option: DialogOption?,
) : Parcelable

sealed interface DialogOption : Parcelable {

    @Parcelize
    data class ActionButton(val id: Int) : DialogOption

    @Parcelize
    data class RadioButton(val data: Parcelable) : DialogOption

}
