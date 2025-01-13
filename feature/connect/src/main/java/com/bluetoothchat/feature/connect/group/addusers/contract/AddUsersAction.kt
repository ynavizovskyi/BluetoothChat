package com.bluetoothchat.feature.connect.group.addusers.contract

import com.bluetoothchat.core.permission.PermissionStatus
import com.bluetoothchat.core.ui.model.ViewBtDeviceWithUser
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface AddUsersAction : ViewAction {

    object BackButtonClicked : AddUsersAction

    object GrantBluetoothPermissionsClicked : AddUsersAction

    object EnableBluetoothClicked : AddUsersAction

    data class OnBluetoothPermissionResult(val status: PermissionStatus) : AddUsersAction

    data class OnEnableBluetoothResult(val enabled: Boolean) : AddUsersAction

    object ScanForDevicesClicked : AddUsersAction

    data class DeviceClicked(val device: ViewBtDeviceWithUser) : AddUsersAction

}
