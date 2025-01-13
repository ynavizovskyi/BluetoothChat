package com.bluetoothchat.feature.connect.group.create.contract

import android.net.Uri
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface CreateGroupAction : ViewAction {

    object BackButtonClicked : CreateGroupAction

    object SaveButtonClicked : CreateGroupAction

    object ChangePhotoClicked : CreateGroupAction

    object DeletePhotoClicked : CreateGroupAction

    data class OnGroupNameChanged(val name: String) : CreateGroupAction

    data class ExternalGalleryContentSelected(val uri: Uri) : CreateGroupAction

}
