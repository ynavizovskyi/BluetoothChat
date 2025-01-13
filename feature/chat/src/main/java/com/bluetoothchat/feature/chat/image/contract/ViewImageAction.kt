package com.bluetoothchat.feature.chat.image.contract

import android.app.Activity
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface ViewImageAction : ViewAction {

    object BackButtonClicked : ViewImageAction

    data class SaveClicked(val activity: Activity) : ViewImageAction

    data class OnWriteStoragePermissionResult(val granted: Boolean) : ViewImageAction

}
