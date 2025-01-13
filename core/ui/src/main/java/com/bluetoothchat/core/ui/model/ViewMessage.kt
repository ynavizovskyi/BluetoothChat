package com.bluetoothchat.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bluetoothchat.core.domain.model.GroupUpdateType
import com.bluetoothchat.core.ui.R

sealed interface ViewMessage {
    val id: String

    //We need both user and the address because in some cases the user might not be present
    val userDeviceAddress: String
    val user: ViewUser?
    val isMine: Boolean
    val timestamp: Long
    val formattedTime: String
    val isReadByMe: Boolean

    data class GroupUpdate(
        override val id: String,
        override val userDeviceAddress: String,
        override val user: ViewUser?,
        override val isMine: Boolean,
        override val timestamp: Long,
        override val formattedTime: String,
        override val isReadByMe: Boolean,
        val updateType: GroupUpdateType,
        val targetUserDeviceAddress: String?,
        val targetUser: ViewUser?,
    ) : ViewMessage

    data class Plain(
        override val id: String,
        override val userDeviceAddress: String,
        override val user: ViewUser?,
        override val isMine: Boolean,
        override val timestamp: Long,
        override val formattedTime: String,
        override val isReadByMe: Boolean,
        val quotedMessage: ViewQuotedMessage?,
        val content: List<ViewMessageContent>,
        val displayUserImage: Boolean,
        val displayUserName: Boolean,
        val actions: List<ViewMessageAction>,
    ) : ViewMessage

}

fun ViewMessage.Plain.primaryContent() = content.primaryContent()

@Composable
fun ViewMessage.GroupUpdate.text() = when (updateType) {
    GroupUpdateType.GROUP_CREATED -> stringResource(
        id = R.string.chat_update_group_created,
        user.toMessageAuthorName(deviceAddress = userDeviceAddress),
    )

    GroupUpdateType.USER_ADDED -> stringResource(
        id = R.string.chat_update_user_added,
        user.toMessageAuthorName(deviceAddress = userDeviceAddress),
        targetUser.toMessageAuthorName(deviceAddress = targetUserDeviceAddress),
    )

    GroupUpdateType.USER_REMOVED -> stringResource(
        id = R.string.chat_update_user_removed,
        user.toMessageAuthorName(deviceAddress = userDeviceAddress),
        targetUser.toMessageAuthorName(deviceAddress = targetUserDeviceAddress),
    )

    GroupUpdateType.USER_LEFT -> stringResource(
        id = R.string.chat_update_user_left,
        targetUser.toMessageAuthorName(deviceAddress = targetUserDeviceAddress),
    )
}
