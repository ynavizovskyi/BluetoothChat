package com.bluetoothchat.feature.main.contract

import com.bluetoothchat.core.permission.BluetoothPermissionType
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent

internal sealed interface MainEvent : ViewOneTimeEvent {

    data class ShowDialog(val params: DialogInputParams) : MainEvent

    data class ShowGrantBluetoothPermissionsSnackbar(val permssionType: BluetoothPermissionType) : MainEvent

    object RequestNotificationPermission : MainEvent

    object RequestBluetoothPermissions : MainEvent

    object ShowGrantNotificationPermissionsSnackbar : MainEvent

    object NavigateToSettings : MainEvent

    object OpenSystemAppSettings : MainEvent

    object LaunchInAppReview : MainEvent

    object NavigateToConnectScreen : MainEvent

    object NavigateToCurrentUserProfileScreen : MainEvent

    data class NavigateToPrivateChatScreen(val chatId: String) : MainEvent

    data class NavigateToGroupChatScreen(val chatId: String) : MainEvent

}
