package com.bluetoothchat.core.db.entity

import androidx.room.Embedded

//fields should have unique names across all classess!
//Because all of these are EMBEDDED
data class DbGroupUpdateMessageData(
    val updateType: DbGroupUpdateType,
    val updateTargetDeviceAddress: String?,
)

data class DbPlainMessageData(
    val plainQuotedMessageId: String?,
    @Embedded val plainTextContent: DbMessageContentText?,
    @Embedded val plainImageContent: DbMessageContentImage?,
)

data class DbMessageContentText(val text: String)

data class DbMessageContentImage(
    val imageFileName: String,
    val imageSizeBytes: Long,
    val imageAspectRatio: Float,
    val imageDominantColor: Int,
)
