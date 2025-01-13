package com.bluetoothchat.feature.connect.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.bluetooth.BtServiceManager
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.message.manager.CommunicationManagerImpl
import com.bluetoothchat.core.bluetooth.message.manager.EstablishConnectionTimeoutMillis
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.ApkExtractor
import com.bluetoothchat.core.permission.PermissionManager
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewBtDeviceWithUser
import com.bluetoothchat.core.ui.model.mapper.ViewBtDeviceMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.feature.connect.destinations.ConnectScreenDestination
import com.bluetoothchat.feature.connect.main.contract.ConnectAction
import com.bluetoothchat.feature.connect.main.contract.ConnectEvent
import com.bluetoothchat.feature.connect.main.contract.ConnectState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import com.bluetoothchat.core.ui.R as CoreUiR

@HiltViewModel
internal class ConnectViewModel @Inject constructor(
    private val btServiceManager: BtServiceManager,
    private val scanner: BtScanner,
    private val communicationManager: CommunicationManagerImpl,
    private val dispatcherManager: DispatcherManager,
    private val userDataSource: UserDataSource,
    private val chatDataSource: ChatDataSource,
    private val btDeviceMapper: ViewBtDeviceMapper,
    private val userMapper: ViewUserMapper,
    private val apkExtractor: ApkExtractor,
    private val analyticsClient: ConnectAnalyticsClient,
    private val permissionManager: PermissionManager,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<ConnectState, ConnectAction, ConnectEvent>(ConnectState.None) {

    private val inputParams = getInputParams(savedStateHandle)

    init {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportScreenShown(
                source = inputParams.source,
                bluetoothEnabled = communicationManager.isBluetoothEnabled(),
            )
        }
        viewModelScope.launch(dispatcherManager.default) {
            when {
                !permissionManager.bluetoothPermissionsGranted() -> setState { ConnectState.NoBluetoothPermission }
                !communicationManager.isBluetoothEnabled() -> setState { ConnectState.BluetoothDisabled }
                else -> onBluetoothReady()
            }
        }
        observePrivateChatStartedEvent()
        observeAddedToGroupEvent()
    }

    private fun getDiscoveringState() = state.value as? ConnectState.Discovering

    override fun handleAction(action: ConnectAction) {
        when (action) {
            is ConnectAction.BackButtonClicked -> handleBackButtonCLicked()
            is ConnectAction.OnResumedStateChanged -> handleResumedStateChanged(action)
            is ConnectAction.GrantBluetoothPermissionsClicked -> handleRequestBluetoothPermissionClicked()
            is ConnectAction.EnableBluetoothClicked -> handleEnableBluetoothClicked()
            is ConnectAction.OnBluetoothPermissionResult -> handleBluetoothPermissionResult(action)
            is ConnectAction.OnEnableBluetoothResult -> handleEnableBluetoothResult(action)
            is ConnectAction.CreateGroupClicked -> handleCreateGroupClicked()
            is ConnectAction.ScanForDevicesClicked -> handleScanForDevicesClicked()
            is ConnectAction.MakeDiscoverableClicked -> handleMakeDiscoverableClicked()
            is ConnectAction.DeviceClicked -> handleDeviceClicked(action)
        }
    }

    private fun handleBackButtonCLicked() {
        sendEvent { ConnectEvent.NavigateBack }
    }

    private fun handleResumedStateChanged(action: ConnectAction.OnResumedStateChanged) {
        if (action.isResumed) {
            communicationManager.onConnectScreenOpen()
        } else {
            communicationManager.onConnectScreenClosed()
        }
    }

    private fun handleCreateGroupClicked() {
        sendEvent { ConnectEvent.NavigateToCreateGroupScreen }
    }

    private fun handleScanForDevicesClicked() {
        startScanning()
    }

    private fun handleMakeDiscoverableClicked() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportMakeDiscoverableClicked()
            sendEvent { ConnectEvent.MakeDeviceDiscoverable }
        }
    }

    private fun handleDeviceClicked(action: ConnectAction.DeviceClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            val deviceAddress = action.device.device.address
            if (chatDataSource.getPrivateChatById(deviceAddress) != null && scanner.isDevicePaired(deviceAddress)) {
                sendEvent { ConnectEvent.NavigateToPrivateChatScreen(deviceAddress) }
            } else {
                val discoveringState = getDiscoveringState() ?: return@launch
                setState { discoveringState.copy(displayProgressOverlay = true) }
                when (action.device.connectionState) {
                    ConnectionState.DISCONNECTED -> {
                        val connected = communicationManager.connect(deviceAddress = deviceAddress)

                        if (connected) {
                            runCatching {
                                withTimeout(EstablishConnectionTimeoutMillis) {
                                    communicationManager.connectionStateFlow.filter {
                                        it.get(deviceAddress) == ConnectionState.CONNECTED
                                    }.first()
                                    communicationManager.inviteUserToPrivateChat(userAddress = deviceAddress)
                                }
                            }.onFailure {
                                onConnectFailed()
                            }
                        } else {
                            onConnectFailed()
                        }
                    }

                    ConnectionState.CONNECTED -> communicationManager.inviteUserToPrivateChat(userAddress = deviceAddress)
                    ConnectionState.CONNECTING -> Unit
                }
            }
        }
    }

    private fun handleRequestBluetoothPermissionClicked() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportGrantBluetoothPermissionClicked()
            sendEvent { ConnectEvent.RequestBluetoothPermission }
        }
    }

    private fun handleEnableBluetoothClicked() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportEnableBluetoothClicked()
            sendEvent { ConnectEvent.RequestEnabledBluetooth }
        }
    }

    private fun handleBluetoothPermissionResult(action: ConnectAction.OnBluetoothPermissionResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.status.isGranted()) {
                analyticsClient.onBluetoothPermissionGranted()
                if (communicationManager.isBluetoothEnabled()) {
                    onBluetoothReady()
                } else {
                    setState { ConnectState.BluetoothDisabled }
                }
            } else {
                analyticsClient.onBluetoothPermissionDenied()
                setState { ConnectState.NoBluetoothPermission }
            }
        }
    }

    private fun handleEnableBluetoothResult(action: ConnectAction.OnEnableBluetoothResult) {
        viewModelScope.launch(dispatcherManager.default) {
            if (action.enabled) {
                analyticsClient.reportBluetoothEnabled()
                onBluetoothReady()
            }
        }
    }

    private fun onBluetoothReady() {
        btServiceManager.ensureStarted()
        observeData()
        if (inputParams.startScanningOnStart) {
            startScanning()
        }
    }

    private fun startScanning() {
        viewModelScope.launch(dispatcherManager.default) {
            scanner.startScanning()
        }
    }

    private fun onConnectFailed() {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportConnectErrorDialogShown()

            val discoveringState = getDiscoveringState() ?: return@launch
            setState { discoveringState.copy(displayProgressOverlay = false) }
            sendEvent {
                ConnectEvent.ShowErrorDialog(
                    params = DialogInputParams.TextDialog(
                        title = UiText.Resource(CoreUiR.string.dialog_error),
                        message = UiText.Resource(CoreUiR.string.connection_error_dialog_message),
                    )
                )
            }
        }
    }

    private fun observeData() {
        combine(
            scanner.stateFlow,
            communicationManager.connectionStateFlow,
            userDataSource.observeAll(),
        ) { scannerState, connectionState, users ->
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
//            }.sortedBy { it.device.connectionState == ConnectionState.CONNECTED }
            val discoveredDevicesWithUsers = discoveredDevices.map { ViewBtDeviceWithUser(device = it, user = null) }

            val discoveringState = getDiscoveringState()
            setState {
                ConnectState.Discovering(
                    pairedDevices = pairedDevicesWithUsers,
                    foundDevices = discoveredDevicesWithUsers,
                    isDeviceDiscoverable = scannerState.isDiscoverable,
                    isScanning = scannerState.isScanning,
                    displayProgressOverlay = discoveringState?.displayProgressOverlay ?: false,
                )
            }
        }
            .launchIn(viewModelScope)
    }

    private fun observePrivateChatStartedEvent() {
        communicationManager.privateChatStartedEventFlow
            .onEach {
                val discoveringState = getDiscoveringState() ?: return@onEach
                setState { discoveringState.copy(displayProgressOverlay = false) }
                sendEvent { ConnectEvent.NavigateToPrivateChatScreen(it) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAddedToGroupEvent() {
        communicationManager.addedToGroupChatEventFlow
            .onEach {
                sendEvent { ConnectEvent.NavigateToGroupChatScreen(it) }
            }
            .launchIn(viewModelScope)
    }

    companion object {

        fun getInputParams(savedStateHandle: SavedStateHandle) =
            requireNotNull(ConnectScreenDestination.argsFrom(savedStateHandle))

    }
}
