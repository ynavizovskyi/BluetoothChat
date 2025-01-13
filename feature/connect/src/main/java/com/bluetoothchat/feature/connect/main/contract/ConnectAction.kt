package com.bluetoothchat.feature.connect.main.contract

import com.bluetoothchat.core.permission.PermissionStatus
import com.bluetoothchat.core.ui.model.ViewBtDevice
import com.bluetoothchat.core.ui.mvi.contract.ViewAction

internal sealed interface ConnectAction : ViewAction {

    object BackButtonClicked : ConnectAction

    data class OnResumedStateChanged(val isResumed: Boolean) : ConnectAction

    object GrantBluetoothPermissionsClicked : ConnectAction

    object EnableBluetoothClicked : ConnectAction

    data class OnBluetoothPermissionResult(val status: PermissionStatus) : ConnectAction

    data class OnEnableBluetoothResult(val enabled: Boolean) : ConnectAction

    object CreateGroupClicked : ConnectAction

    object ScanForDevicesClicked : ConnectAction

    object MakeDiscoverableClicked : ConnectAction

    data class DeviceClicked(val device: ViewBtDevice) : ConnectAction

}
