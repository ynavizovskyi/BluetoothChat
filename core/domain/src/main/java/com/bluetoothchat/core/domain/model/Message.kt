package com.bluetoothchat.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Message : Parcelable {
    val id: String
    val userDeviceAddress: String
    val timestamp: Long
    val isReadByMe: Boolean

    fun copyInstance(timestamp: Long = this.timestamp, isReadByMe: Boolean = this.isReadByMe): Message

    @Parcelize
    data class GroupUpdate(
        override val id: String,
        override val userDeviceAddress: String,
        override val timestamp: Long,
        override val isReadByMe: Boolean,
        val updateType: GroupUpdateType,
        val targetDeviceAddress: String?,
    ) : Message {
        override fun copyInstance(timestamp: Long, isReadByMe: Boolean) =
            copy(timestamp = timestamp, isReadByMe = isReadByMe)
    }

    @Parcelize
    data class Plain(
        override val id: String,
        override val userDeviceAddress: String,
        override val timestamp: Long,
        override val isReadByMe: Boolean,
        val quotedMessageId: String?,
        val content: List<MessageContent>,
    ) : Message {
        override fun copyInstance(timestamp: Long, isReadByMe: Boolean) =
            copy(timestamp = timestamp, isReadByMe = isReadByMe)
    }

}
