package com.bluetoothchat.core.ui.components.dialog.model

import android.os.Parcelable
import com.bluetoothchat.core.ui.model.UiText
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface DialogInputParams : Parcelable {
    val id: Int
    val title: UiText
    val confirmButton: DialogButton
    val actionPayload: Parcelable?

    @Parcelize
    data class TextDialog(
        override val id: Int = 0,
        override val title: UiText,
        override val confirmButton: DialogButton = defaultDialogButton(),
        override val actionPayload: Parcelable? = null,
        val message: UiText,
        val cancelButton: DialogButton? = null,
    ) : DialogInputParams

    @Parcelize
    data class RadioGroupDialog(
        override val id: Int = 0,
        override val title: UiText,
        override val confirmButton: DialogButton = defaultDialogButton(),
        override val actionPayload: Parcelable? = null,
        val radioButtons: List<DialogRadioButton>,
    ) : DialogInputParams

}
