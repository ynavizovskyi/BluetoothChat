package com.bluetoothchat.core.ui.components.dialog.model

import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.model.UiText

val InvalidImageDialogInputParams = DialogInputParams.TextDialog(
    title = UiText.Resource(R.string.dialog_error),
    message = UiText.Resource(R.string.error_invalid_image_message),
)
