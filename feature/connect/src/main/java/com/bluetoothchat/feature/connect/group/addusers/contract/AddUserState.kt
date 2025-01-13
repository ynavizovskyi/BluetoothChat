package com.bluetoothchat.feature.connect.group.addusers.contract

import com.bluetoothchat.core.ui.mvi.contract.ViewState
import com.bluetoothchat.feature.connect.group.addusers.data.ViewBtDeviceWithUserWithMembership

internal sealed interface AddUserState : ViewState {

    object None : AddUserState

    object NoBluetoothPermission : AddUserState

    object BluetoothDisabled : AddUserState

    data class Discovering(
        val foundDevices: List<ViewBtDeviceWithUserWithMembership>,
        val pairedDevices: List<ViewBtDeviceWithUserWithMembership>,
        val isScanning: Boolean,
        val displayProgressOverlay: Boolean,
    ) : AddUserState

}
