package com.bluetoothchat.feature.chat.group.chatinfo.contract

import android.net.Uri
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.ViewUserAction
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface GroupChatInfoAction : ViewAction {

    object BackButtonClicked : GroupChatInfoAction

    object AddMembersClicked : GroupChatInfoAction

    data class UserClicked(val user: ViewUser) : GroupChatInfoAction

    data class UserActionClicked(val user: ViewUser, val action: ViewUserAction) : GroupChatInfoAction

    object EditClicked : GroupChatInfoAction

    object SaveClicked : GroupChatInfoAction

    object ChangePhotoClicked : GroupChatInfoAction

    object DeletePhotoClicked : GroupChatInfoAction

    data class ExternalGalleryContentSelected(val uri: Uri) : GroupChatInfoAction

    data class OnUsernameChanged(val name: String) : GroupChatInfoAction

}
