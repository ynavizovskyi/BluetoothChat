package com.bluetoothchat.core.ui.model

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.filemanager.file.FileState

sealed interface ViewChat {

    val id: String
    val name: String
    val createdTimestamp: Long
    val formattedTime: String
    val exists: Boolean

    data class Private(
        override val createdTimestamp: Long,
        override val formattedTime: String,
        override val exists: Boolean,
        val user: ViewUser,
        val actions: List<ViewChatAction>,
    ) : ViewChat {
        override val id get() = user.deviceAddress
        override val name get() = user.userName ?: user.deviceAddress
    }

    data class Group(
        override val id: String,
        override val name: String,
        override val createdTimestamp: Long,
        override val formattedTime: String,
        override val exists: Boolean,
        val hostDeviceAddress: String,
        val connectedMembersNumber: Int?,
        val pictureFileState: FileState?,
        val users: List<ViewUser>,
        val actions: List<ViewChatAction>,
    ) : ViewChat

}

fun ViewChat.toDomain() = when (this) {
    is ViewChat.Private -> this.toDomain()
    is ViewChat.Group -> this.toDomain()
}

fun ViewChat.Private.toDomain() = Chat.Private(
    createdTimestamp = createdTimestamp,
    exists = exists,
    user = user.toDomain(),
)

fun ViewChat.Group.toDomain() = Chat.Group(
    id = id,
    createdTimestamp = createdTimestamp,
    exists = exists,
    hostDeviceAddress = hostDeviceAddress,
    name = name,
    picture  = pictureFileState?.toPictureDomain(),
    users = users.map { it.toDomain() },
)
