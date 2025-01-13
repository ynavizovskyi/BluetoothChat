package com.bluetoothchat.core.ui.model

data class ViewQuotedMessage(
    val id: String,
    val userDeviceAddress: String,
    val primaryContent: ViewMessageContent?,
    val user: ViewUser?,
)
