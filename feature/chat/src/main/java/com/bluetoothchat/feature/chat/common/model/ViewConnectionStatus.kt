package com.bluetoothchat.feature.chat.common.model

sealed interface ViewConnectionStatus {

    object Disconnected : ViewConnectionStatus

    object Connecting : ViewConnectionStatus

    object Connected : ViewConnectionStatus

}
