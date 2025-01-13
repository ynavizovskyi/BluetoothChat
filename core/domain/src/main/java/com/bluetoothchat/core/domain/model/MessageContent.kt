package com.bluetoothchat.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface MessageContent : Parcelable {

    @Parcelize
    data class Text(val text: String) : MessageContent

    sealed interface File : MessageContent {
        val fileName: String
        val fileSizeBytes: Long

        @Parcelize
        data class Image(
            override val fileName: String,
            override val fileSizeBytes: Long,
            val aspectRatio: Float,
            val dominantColor: Int,
        ) : File
    }

}

fun List<MessageContent>.primary() = first()

//TODO: remove hardcoded string
fun MessageContent.toShortDescription() = when (this) {
    is MessageContent.Text -> text
    is MessageContent.File.Image -> "Image"
}

enum class GroupUpdateType {
    GROUP_CREATED, USER_ADDED, USER_REMOVED, USER_LEFT,
}
