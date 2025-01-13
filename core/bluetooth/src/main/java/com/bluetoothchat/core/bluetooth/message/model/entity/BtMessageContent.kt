package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface BtMessageContent {

    @Serializable
    @SerialName("contentText")
    data class Text(val text: String) : BtMessageContent

    sealed interface File : BtMessageContent {
        val fileName: String
        val fileSizeBytes: Long

        @Serializable
        @SerialName("contentImage")
        data class Image(
            override val fileName: String,
            override val fileSizeBytes: Long,
            val aspectRatio: Float,
            val dominantColor: Int,
        ) : File
    }

}
