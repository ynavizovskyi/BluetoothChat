package com.bluetoothchat.core.ui.mvi.action

import androidx.annotation.StringRes

sealed interface ViewActionConfirmation {
    object None : ViewActionConfirmation

    data class Dialog(
        @StringRes val titleStringRes: Int,
        @StringRes val messageStringRes: Int,
        @StringRes val actionStringRes: Int,
    ) : ViewActionConfirmation
}
