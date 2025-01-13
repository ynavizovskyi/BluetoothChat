package com.bluetoothchat.core.ui.components.dialog.model

import android.os.Parcelable
import com.bluetoothchat.core.ui.model.UiText
import kotlinx.parcelize.Parcelize

@Parcelize
data class DialogRadioButton(val text: UiText, val data: Parcelable, val isSelected: Boolean) : Parcelable
