package com.bluetoothchat.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.theme.Colors

data class ViewUser(
    val deviceAddress: String,
    val color: Int,
    val isConnected: Boolean,
    val deviceName: String?,
    val userName: String?,
    val pictureFileState: FileState?,
    val actions: List<ViewUserAction>,
    val isMe: Boolean,
)

fun ViewUser.toDomain() = User(
    deviceAddress = deviceAddress,
    color = color,
    deviceName = deviceName,
    userName = userName,
    picture  = pictureFileState?.toPictureDomain(),
)

fun ViewUser?.toColor() = this?.let { Color(it.color) } ?: Colors.userColor1

@Composable
fun ViewUser?.toMessageAuthorName(deviceAddress: String?) = if (this?.isMe == true) {
    stringResource(id = R.string.chat_you)
} else {
    this?.userName ?: deviceAddress ?: ""
}
