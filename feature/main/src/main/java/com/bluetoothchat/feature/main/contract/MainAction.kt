package com.bluetoothchat.feature.main.contract

import com.bluetoothchat.core.permission.BluetoothPermissions
import com.bluetoothchat.core.permission.PermissionStatus
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.model.ViewChat
import com.bluetoothchat.core.ui.model.ViewChatAction
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface MainAction : ViewAction {

    object ProfileClicked : MainAction

    object SettingsClicked : MainAction

    object OpenSystemAppSettingsClicked : MainAction

    object CreateNewButtonClicked : MainAction

    data class OnBluetoothPermissionsStatusChanged(
        val status: PermissionStatus,
        val permissions: BluetoothPermissions,
    ) : MainAction

    data class OnNotificationPermissionStatusChanged(val status: PermissionStatus) : MainAction

    data class OnDialogResult(val result: DialogResult) : MainAction

    data class ChatClicked(val chat: ViewChat) : MainAction

    data class ChatActionClicked(val chat: ViewChat, val action: ViewChatAction) : MainAction

    data class OnLaunchInAppReviewResult(val result: Boolean) : MainAction

}
