package com.bluetoothchat.feature.profile.contract

import android.net.Uri
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal interface ProfileAction : ViewAction {

    object BackButtonClicked : ProfileAction

    object EditClicked : ProfileAction

    object SaveClicked : ProfileAction

    object ChangePhotoClicked : ProfileAction

    object DeletePhotoClicked : ProfileAction

    data class ExternalGalleryContentSelected(val uri: Uri) : ProfileAction

    data class OnUsernameChanged(val name: String) : ProfileAction

    data class OnColorChanged(val color: Int) : ProfileAction

}
