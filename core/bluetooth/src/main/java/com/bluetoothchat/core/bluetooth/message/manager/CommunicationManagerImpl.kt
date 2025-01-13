package com.bluetoothchat.core.bluetooth.message.manager

import com.bluetoothchat.core.bluetooth.connection.BtConnectionManager
import com.bluetoothchat.core.bluetooth.message.ConnectionState
import com.bluetoothchat.core.bluetooth.message.manager.delegate.ConnectionDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.FileDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.GroupChatDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.NotificationDelegateImpl
import com.bluetoothchat.core.bluetooth.message.manager.delegate.PrivateChatDelegateImpl
import com.bluetoothchat.core.bluetooth.message.model.BtParseMessageResult
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.session.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationManagerImpl @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val connectionManager: BtConnectionManager,
    private val privateChatManager: PrivateChatDelegateImpl,
    private val groupChatManager: GroupChatDelegateImpl,
    private val fileDelegate: FileDelegateImpl,
    private val connectionDelegate: ConnectionDelegateImpl,
    private val notificationDelegate: NotificationDelegateImpl,
    private val session: Session,
) : CommunicationManager, ConnectionDelegate by connectionDelegate, FileDelegate by fileDelegate,
    NotificationDelegate by notificationDelegate, PrivateChatDelegate by privateChatManager,
    GroupChatDelegate by groupChatManager {

    override val connectedDevicesFlow: Flow<List<String>> = connectionManager.connectedDeviceIdsFlow
        .onStart { emit(emptyList()) }
    override val connectionStateFlow = combine(
        connectionDelegate.connectingDevicesFlow,
        connectionManager.connectedDeviceIdsFlow.onStart { emit(emptyList()) },
    ) { connectingDevices, connectedDevices ->
        val connectingDevicesMap = connectingDevices.associateWith { ConnectionState.CONNECTING }
        val connectedDevicesMap = connectedDevices.filterNot { connectingDevices.contains(it) }
            .associateWith { ConnectionState.CONNECTED }

        connectingDevicesMap + connectedDevicesMap
    }

    init {
        connectionManager.messagesFlow
            .onEach {
                handleMessage(parseResult = it.message, deviceAddress = it.deviceAddress)
            }
            .flowOn(dispatcherManager.io)
            .launchIn(applicationScope)
    }

    override suspend fun ensureStarted() {
        connectionDelegate.listenForConnectionRequests()
        connectionManager.ensureStarted()

        session.setDeviceNameIfChanged(connectionManager.getMyDeviceName())
    }

    private suspend fun handleMessage(parseResult: BtParseMessageResult, deviceAddress: String) {
        when (parseResult) {
            is BtParseMessageResult.Error.IncompatibleProtocols -> {
                connectionDelegate.handleIncompatibleProtocolsError(error = parseResult, deviceAddress = deviceAddress)
            }

            is BtParseMessageResult.Success -> {
                when (val message = parseResult.message) {
                    is Protocol.InitConnection -> connectionDelegate.handleMessage(
                        message = message,
                        deviceAddress = deviceAddress
                    )

                    is Protocol.File -> fileDelegate.handleMessage(message = message, deviceAddress = deviceAddress)
                    is Protocol.GroupChat -> groupChatManager.handleMessage(
                        message = message,
                        deviceAddress = deviceAddress
                    )

                    is Protocol.PrivateChat -> privateChatManager.handleMessage(
                        message = message,
                        deviceAddress = deviceAddress
                    )
                }
            }
        }
    }

}
