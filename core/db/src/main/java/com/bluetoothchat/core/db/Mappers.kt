package com.bluetoothchat.core.db

import com.bluetoothchat.core.db.entity.DbGroupChat
import com.bluetoothchat.core.db.entity.DbGroupUpdateMessageData
import com.bluetoothchat.core.db.entity.DbGroupUpdateType
import com.bluetoothchat.core.db.entity.DbMessage
import com.bluetoothchat.core.db.entity.DbMessageContentImage
import com.bluetoothchat.core.db.entity.DbMessageContentText
import com.bluetoothchat.core.db.entity.DbPicture
import com.bluetoothchat.core.db.entity.DbPlainMessageData
import com.bluetoothchat.core.db.entity.DbPrivateChat
import com.bluetoothchat.core.db.entity.DbUser
import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.domain.model.User

internal fun Chat.Group.toEntity() = DbGroupChat(
    id = id,
    createdTimestamp = createdTimestamp,
    exist = exists,
    hostDeviceAddress = hostDeviceAddress,
    name = name,
    picture = picture?.toEntity(),
)

internal fun DbGroupChat.toDomain(users: List<User>) = Chat.Group(
    id = id,
    exists = exist,
    createdTimestamp = createdTimestamp,
    hostDeviceAddress = hostDeviceAddress,
    name = name,
    picture = picture?.toDomain(),
    users = users,
)

internal fun Chat.Private.toEntity() = DbPrivateChat(
    userDeviceAddress = user.deviceAddress,
    exist = exists,
    createdTimestamp = createdTimestamp,
)

internal fun DbPrivateChat.toDomain(user: User) = Chat.Private(
    createdTimestamp = createdTimestamp,
    exists = exist,
    user = user,
)

internal fun User.toEntity() = DbUser(
    deviceAddress = deviceAddress,
    color = color,
    deviceName = deviceName,
    userName = userName,
    picture = picture?.toEntity(),
)

internal fun DbUser.toDomain() = User(
    deviceAddress = deviceAddress,
    color = color,
    deviceName = deviceName,
    userName = userName,
    picture = picture?.toDomain(),
)

internal fun Picture.toEntity() = DbPicture(pictureId = id, pictureSizeBytes = sizeBytes)

internal fun DbPicture.toDomain() = Picture(id = pictureId, sizeBytes = pictureSizeBytes)

internal fun Message.toEntity(chatId: String): DbMessage {
    when (this) {
        is Message.GroupUpdate -> {
            val groupUpdateData = DbGroupUpdateMessageData(
                updateType = updateType.toEntity(),
                updateTargetDeviceAddress = this.targetDeviceAddress
            )
            return DbMessage(
                id = id,
                chatId = chatId,
                userDeviceAddress = userDeviceAddress,
                timestamp = timestamp,
                isReadByMe = isReadByMe,
                groupUpdateData = groupUpdateData,
                plainData = null,
            )
        }

        is Message.Plain -> {
            val textContent = content.filterIsInstance<MessageContent.Text>().firstOrNull()?.let {
                DbMessageContentText(text = it.text)
            }
            val imageContent = content.filterIsInstance<MessageContent.File.Image>().firstOrNull()?.let {
                DbMessageContentImage(
                    imageFileName = it.fileName,
                    imageSizeBytes = it.fileSizeBytes,
                    imageAspectRatio = it.aspectRatio,
                    imageDominantColor = it.dominantColor,
                )
            }
            val plainData = DbPlainMessageData(
                plainQuotedMessageId = this.quotedMessageId,
                plainTextContent = textContent,
                plainImageContent = imageContent,
            )

            return DbMessage(
                id = id,
                chatId = chatId,
                userDeviceAddress = userDeviceAddress,
                timestamp = timestamp,
                isReadByMe = isReadByMe,
                groupUpdateData = null,
                plainData = plainData,
            )
        }
    }
}

internal fun DbMessage.toDomain(): Message {
    return when {
        groupUpdateData != null -> {
            Message.GroupUpdate(
                id = id,
                userDeviceAddress = userDeviceAddress,
                timestamp = timestamp,
                isReadByMe = isReadByMe,
                updateType = groupUpdateData.updateType.toDomain(),
                targetDeviceAddress = groupUpdateData.updateTargetDeviceAddress,
            )
        }

        plainData != null -> {
            val textContent = plainData.plainTextContent?.let {
                MessageContent.Text(text = it.text)
            }
            val imageContent = plainData.plainImageContent?.let {
                MessageContent.File.Image(
                    fileName = it.imageFileName,
                    fileSizeBytes = it.imageSizeBytes,
                    aspectRatio = it.imageAspectRatio,
                    dominantColor = it.imageDominantColor
                )
            }
            val content = listOf(textContent, imageContent).filterNotNull()
            return Message.Plain(
                id = id,
                userDeviceAddress = userDeviceAddress,
                timestamp = timestamp,
                isReadByMe = isReadByMe,
                quotedMessageId = plainData.plainQuotedMessageId,
                content = content,
            )
        }

        else -> error("All of the message data fields are null: $this")
    }
}

internal fun GroupUpdateType.toEntity() = when (this) {
    GroupUpdateType.GROUP_CREATED -> DbGroupUpdateType.GROUP_CREATED
    GroupUpdateType.USER_ADDED -> DbGroupUpdateType.USER_ADDED
    GroupUpdateType.USER_REMOVED -> DbGroupUpdateType.USER_REMOVED
    GroupUpdateType.USER_LEFT -> DbGroupUpdateType.USER_LEFT
}

internal fun DbGroupUpdateType.toDomain() = when (this) {
    DbGroupUpdateType.GROUP_CREATED -> GroupUpdateType.GROUP_CREATED
    DbGroupUpdateType.USER_ADDED -> GroupUpdateType.USER_ADDED
    DbGroupUpdateType.USER_REMOVED -> GroupUpdateType.USER_REMOVED
    DbGroupUpdateType.USER_LEFT -> GroupUpdateType.USER_LEFT
}
