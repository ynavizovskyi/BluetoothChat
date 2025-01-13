package com.bluetoothchat.core.ui.model.mapper

import android.util.Log
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.model.ViewUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewUserMapper @Inject constructor(private val fileManager: FileManager, private val session: Session) {

    suspend fun map(
        user: User,
        connectedDevices: List<String>,
        userActionsMapper: ViewUserActionsMapper? = null,
    ): ViewUser {
        val isConnected = connectedDevices.contains(user.deviceAddress)
        return ViewUser(
            deviceAddress = user.deviceAddress,
            color = user.color,
            isConnected = isConnected,
            deviceName = user.deviceName,
            userName = user.userName,
            pictureFileState = user.picture?.let {
                fileManager.getChatAvatarPictureFile(
                    fileName = it.id,
                    sizeBytes = it.sizeBytes,
                )
            },
            actions = userActionsMapper?.map(user = user, isConnected = isConnected) ?: emptyList(),
            isMe = session.isCurrentUser(deviceAddress = user.deviceAddress),
        )
    }

}
