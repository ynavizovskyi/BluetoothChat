package com.bluetoothchat.feature.connect.group.addusers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.permission.PermissionManager
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.UiTextParam
import com.bluetoothchat.core.ui.model.UiTextParamStyle
import com.bluetoothchat.core.ui.model.ViewBtDeviceWithUser
import com.bluetoothchat.core.ui.model.mapper.ViewBtDeviceMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.connect.destinations.AddUsersScreenDestination
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUserEvent
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUserState
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUsersAction
import com.bluetoothchat.feature.connect.group.addusers.data.ViewBtDeviceWithUserWithMembership
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
internal class AddUserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val btServiceManager: BtServiceManager,
    private val scanner: BtScanner,
    private val chatDataSource: ChatDataSource,
    private val userDataSource: UserDataSource,
    private val btDeviceMapper: ViewBtDeviceMapper,
    private val userMapper: ViewUserMapper,
    private val communicationManager: CommunicationManagerImpl,
    private val dispatcherManager: DispatcherManager,
    private val permissionManager: PermissionManager,
    private val analyticsClient: AddUsersAnalyticsClient,
) : MviViewModel<AddUserState, AddUsersAction, AddUserEvent>(AddUserState.None) {

    val inputParams = getInputParams(savedStateHandle)

    init {
        viewModelScope.launch(dispatcherManager.default) {
            val chat = chatDataSource.getGroupChatById(chatId = inputParams.chatId)
            if (chat != null) {
                analyticsClient.reportScreenShown(chat = chat)
            }
        }
        viewModelScope.launch(dispatcherManager.default) {
            when {
                !permissionManager.bluetoothPermissionsGranted() -> setState { AddUserState.NoBluetoothPermission }
                !communicationManager.isBluetoothEnabled() -> setState { AddUserState.BluetoothDisabled }
                else -> onBluetoothReady()
            }
        }
    }

    private fun getDiscoveringState() = state.value as? AddUserState.Discovering

    override fun handleAction(action: AddUsersAction) {
        when (action) {
            is AddUsersAction.BackButtonClicked -> handleBackButtonClicked()
            is AddUsersAction.GrantBluetoothPermissionsClicked -> handleRequestBluetoothPermissionClicked()
            is AddUsersAction.EnableBluetoothClicked -> handleEnableBluetoothClicked()
            is AddUsersAction.OnBluetoothPermissionResult -> handleBluetoothPermissionResult(action)
            is AddUsersAction.OnEnableBluetoothResult -> handleEnableBluetoothResult(action)
            is AddUsersAction.ScanForDevicesClicked -> handleScanForDevicesClicked()
            is AddUsersAction.DeviceClicked -> handleDeviceClicked(action)
        }
    }

    private fun handleBackButtonClicked() {
        sendEvent { AddUserEvent.NavigateBack }
    }

    private fun handleRequestBluetoothPermissionClicked() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportGrantBluetoothPermissionClicked()
            sendEvent { AddUserEvent.RequestBluetoothPermission }
        }
    }

    private fun handleEnableBluetoothClicked() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportEnableBluetoothClicked()
            sendEvent { AddUserEvent.RequestEnabledBluetooth }
        }
    }

    private fun handleBluetoothPermissionResult(action: AddUsersAction.OnBluetoothPermissionResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.status.isGranted()) {
                analyticsClient.onBluetoothPermissionGranted()
                if (communicationManager.isBluetoothEnabled()) {
                    onBluetoothReady()
                } else {
                    setState { AddUserState.BluetoothDisabled }
                }
            } else {
                analyticsClient.onBluetoothPermissionDenied()
                setState { AddUserState.NoBluetoothPermission }
            }
        }
    }

    private fun handleEnableBluetoothResult(action: AddUsersAction.OnEnableBluetoothResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.enabled) {
                analyticsClient.reportBluetoothEnabled()
                onBluetoothReady()
            }
        }
    }

    private fun handleScanForDevicesClicked() {
        startScanning()
    }

    private fun onBluetoothReady() {
        btServiceManager.ensureStarted()
        observeData()
        startScanning()
    }

    private fun handleDeviceClicked(action: AddUsersAction.DeviceClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            val deviceAddress = action.device.device.device.address
            val chat = chatDataSource.getGroupChatById(inputParams.chatId) ?: return@launch
            if (chat.users.any { it.deviceAddress == deviceAddress }) {
                val user = action.device.user
                sendEvent {
                    AddUserEvent.ShowErrorDialog(
                        params = DialogInputParams.TextDialog(
                            title = UiText.Resource(R.string.dialog_error),
                            message = UiText.Resource(
                                resId = R.string.user_already_a_member_dialog_message,
                                params = listOf(
                                    UiTextParam(
                                        text = (action.device.device.device.name ?: "")
                                                + (user?.let { " (${it.userName})" } ?: ""),
                                        style = UiTextParamStyle.BOLD),
                                )),
                        )
                    )
                }
            } else {
                val discoveringState = getDiscoveringState() ?: return@launch
                setState { discoveringState.copy(displayProgressOverlay = true) }
                when (action.device.device.connectionState) {
                    ConnectionState.DISCONNECTED -> {
                        val connected = communicationManager.connect(deviceAddress = deviceAddress)

                        if (connected) {
                            runCatching {
                                withTimeout(ESTABLISH_CONNECTION_TIMEOUT_MILLIS) {
                                    communicationManager.connectionStateFlow.filter {
                                        it.get(deviceAddress) == ConnectionState.CONNECTED
                                    }.first()
                                    communicationManager.addUserToChat(
                                        chatId = inputParams.chatId,
                                        userAddress = action.device.device.device.address
                                    )
                                    //A hacky way of giving the app time to sync data after connecting
                                    viewModelScope.launch(dispatcherManager.default) {
                                        delay(CONNECT_HIDE_PROGRESS_DELAY_MILLIS)
                                        val discoveringState = getDiscoveringState() ?: return@launch

                                        analyticsClient.reportMemberAdded()
                                        setState { discoveringState.copy(displayProgressOverlay = false) }
                                    }
                                }
                            }.onFailure {
                                onConnectFailed()
                            }
                        } else {
                            onConnectFailed()
                        }
                    }

                    ConnectionState.CONNECTED -> {
                        communicationManager.addUserToChat(
                            chatId = inputParams.chatId,
                            userAddress = action.device.device.device.address
                        )
                        //A hacky way of giving the app time to sync data after connecting
                        viewModelScope.launch(dispatcherManager.default) {
                            delay(CONNECT_HIDE_PROGRESS_DELAY_MILLIS)
                            val discoveringState = state.value as? AddUserState.Discovering ?: return@launch

                            analyticsClient.reportMemberAdded()
                            setState { discoveringState.copy(displayProgressOverlay = false) }
                        }
                    }

                    ConnectionState.CONNECTING -> Unit
                }
            }
        }
    }

    private fun onConnectFailed() {
        val discoveringState = getDiscoveringState() ?: return

        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportConnectErrorDialogShown()

            setState { discoveringState.copy(displayProgressOverlay = false) }
            sendEvent {
                AddUserEvent.ShowErrorDialog(
                    params = DialogInputParams.TextDialog(
                        title = UiText.Resource(R.string.dialog_error),
                        message = UiText.Resource(R.string.connection_error_dialog_message),
                    )
                )
            }
        }
    }

    private fun startScanning() {
        viewModelScope.launch {
            scanner.startScanning()
        }
    }

    private fun observeData() {
        combine(
            chatDataSource.observeGroupChatById(inputParams.chatId),
            scanner.stateFlow,
            communicationManager.connectionStateFlow,
            userDataSource.observeAll(),
        ) { chat, scannerState, connectionState, users ->
            val viewUser = users.map {
                userMapper.map(
                    user = it,
                    connectedDevices = connectionState.filter { it.value == ConnectionState.CONNECTED }.keys.toList(),
                )
            }
            val pairedDevices = scannerState.pairedDevices.map { device ->
                btDeviceMapper.map(device = device, connectionsStates = connectionState)
            }
            val discoveredDevices = (scannerState.foundDevices - scannerState.pairedDevices).map { device ->
                btDeviceMapper.map(device = device, connectionsStates = connectionState)
            }

            val pairedDevicesWithUsers = pairedDevices.map { device ->
                ViewBtDeviceWithUser(
                    device = device,
                    user = viewUser.firstOrNull { it.deviceAddress == device.device.address },
                )
            }.sortedWith(compareBy({ it.device.connectionState }, { it.user == null }, { it.user?.userName }))
            val discoveredDevicesWithUsers = discoveredDevices.map { ViewBtDeviceWithUser(device = it, user = null) }

            val discoveringState = state.value as? AddUserState.Discovering
            setState {
                AddUserState.Discovering(
                    pairedDevices = pairedDevicesWithUsers.map { deviceWithUser ->
                        ViewBtDeviceWithUserWithMembership(
                            deviceWithUser = deviceWithUser,
                            isMember = chat?.users?.any { it.deviceAddress == deviceWithUser.user?.deviceAddress }
                                ?: false,
                        )
                    },
                    foundDevices = discoveredDevicesWithUsers.map { deviceWithUser ->
                        ViewBtDeviceWithUserWithMembership(
                            deviceWithUser = deviceWithUser,
                            isMember = chat?.users?.any { it.deviceAddress == deviceWithUser.user?.deviceAddress }
                                ?: false,
                        )
                    },
                    isScanning = scannerState.isScanning,
                    displayProgressOverlay = discoveringState?.displayProgressOverlay ?: false,
                )
            }
        }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val ESTABLISH_CONNECTION_TIMEOUT_MILLIS = 20_000L
        private const val CONNECT_HIDE_PROGRESS_DELAY_MILLIS = 2_000L

        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(AddUsersScreenDestination.argsFrom(savedStateHandle))
    }
}
