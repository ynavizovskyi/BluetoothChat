package com.bluetoothchat.core.bluetooth.message.model.entity

import com.bluetoothchat.core.domain.model.Chat
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.domain.model.MessageContent
import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.domain.model.User

internal fun User.toBluetooth() = BtUser(
    deviceAddress = deviceAddress,
    color = color,
    deviceName = deviceName,
    userName = userName,
    picture = picture?.toBluetooth(),
)

internal fun BtUser.toDomain() = User(
    deviceAddress = deviceAddress,
    color = color,
    deviceName = deviceName,
    userName = userName,
    picture = picture?.toDomain(),
)

internal fun Picture.toBluetooth() = BtPicture(id = id, sizeBytes = sizeBytes)

internal fun BtPicture.toDomain() = Picture(id = id, sizeBytes = sizeBytes)

internal fun GroupUpdateType.toBluetooth() = when (this) {
    GroupUpdateType.GROUP_CREATED -> BtGroupUpdateType.GROUP_CREATED
    GroupUpdateType.USER_ADDED -> BtGroupUpdateType.USER_ADDED
    GroupUpdateType.USER_REMOVED -> BtGroupUpdateType.USER_REMOVED
    GroupUpdateType.USER_LEFT -> BtGroupUpdateType.USER_LEFT
}

internal fun BtGroupUpdateType.toDomain() = when (this) {
    BtGroupUpdateType.GROUP_CREATED -> GroupUpdateType.GROUP_CREATED
    BtGroupUpdateType.USER_ADDED -> GroupUpdateType.USER_ADDED
    BtGroupUpdateType.USER_REMOVED -> GroupUpdateType.USER_REMOVED
    BtGroupUpdateType.USER_LEFT -> GroupUpdateType.USER_LEFT
}

internal fun MessageContent.toBluetooth() = when (this) {
    is MessageContent.Text -> BtMessageContent.Text(text = text)
    is MessageContent.File.Image -> BtMessageContent.File.Image(
        fileName = fileName,
        fileSizeBytes = fileSizeBytes,
        aspectRatio = aspectRatio,
        dominantColor = dominantColor,
    )
}

internal fun BtMessageContent.toDomain() = when (this) {
    is BtMessageContent.Text -> MessageContent.Text(text = text)
    is BtMessageContent.File.Image -> MessageContent.File.Image(
        fileName = fileName,
        fileSizeBytes = fileSizeBytes,
        aspectRatio = aspectRatio,
        dominantColor = dominantColor,
    )
}

internal fun BtMessage.toDomain() = when (this) {
    is BtMessage.GroupUpdate -> {
        Message.GroupUpdate(
            id = id,
            userDeviceAddress = userDeviceAddress,
            timestamp = timestamp,
            isReadByMe = isReadByMe,
            updateType = updateType.toDomain(),
            targetDeviceAddress = targetDeviceAddress,
        )
    }

    is BtMessage.Plain -> {
        Message.Plain(
            id = id,
            userDeviceAddress = userDeviceAddress,
            timestamp = timestamp,
            isReadByMe = isReadByMe,
            quotedMessageId = quotedMessageId,
            content = content.map { it.toDomain() },
        )
    }
}

internal fun Message.toBluetooth() = when (this) {
    is Message.GroupUpdate -> {
        BtMessage.GroupUpdate(
            id = id,
            userDeviceAddress = userDeviceAddress,
            timestamp = timestamp,
            isReadByMe = isReadByMe,
            updateType = updateType.toBluetooth(),
            targetDeviceAddress = targetDeviceAddress,
        )
    }

    is Message.Plain -> {
        BtMessage.Plain(
            id = id,
            userDeviceAddress = userDeviceAddress,
            timestamp = timestamp,
            isReadByMe = isReadByMe,
            quotedMessageId = quotedMessageId,
            content = content.map { it.toBluetooth() },
        )
    }
}

internal fun BtGroupChat.toDomain() = Chat.Group(
    id = id,
    createdTimestamp = createdTimestamp,
    exists = exists,
    hostDeviceAddress = hostDeviceAddress,
    name = name,
    picture = picture?.toDomain(),
    users = users.map { it.toDomain() },
)

internal fun Chat.Group.toBluetooth() = BtGroupChat(
    id = id,
    createdTimestamp = createdTimestamp,
    exists = exists,
    hostDeviceAddress = hostDeviceAddress,
    name = name,
    picture = picture?.toBluetooth(),
    users = users.map { it.toBluetooth() },
    connectedDevicesIds = emptySet(), //always empty for now; futureproofing the protocol
)
