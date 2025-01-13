package com.bluetoothchat.core.bluetooth.connection

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.scanner.BtScanner
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.file.FileManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

//TODO: concurrency issues here because of the map
//https://stackoverflow.com/questions/68473491/under-which-circumstances-can-toset-throw-an-java-lang-illegalargumentexception
@Singleton
class BtConnectionManager @Inject constructor(
    private val btScanner: BtScanner,
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val fileManager: FileManager,
    private val messageManager: MessageManager,
) {

    private val updateConnectionsMutex = Mutex()
    private val connections = mutableMapOf<String, BtConnection>()

    private val _messagesChannel: Channel<BtMessageWithAddress> = Channel(Channel.BUFFERED)
    internal val messagesFlow = _messagesChannel.receiveAsFlow()

    private val _fileDownloadedEventChannel: MutableSharedFlow<BtConnection.FileDownloadedEvent> = MutableSharedFlow()
    internal val fileDownloadedEventFlow = _fileDownloadedEventChannel

    private val _connectionState = MutableStateFlow(emptySet<BtConnection>())
    internal val connectionState: Flow<Set<BtConnection>> = _connectionState

    internal fun isBluetoothEnabled() = btScanner.isBluetoothEnabled()

    internal val connectedDevicesFlow = connectionState.flatMapLatest { connections ->
        combine(connections.map { it.state }) { it.toList() }
    }.map { connectionStates ->
        connectionStates.filter { it !is BtConnection.State.Disconnected }
    }
    internal val connectedDeviceIdsFlow = connectedDevicesFlow.map { it.map { it.deviceAddress } }
    internal val connectedDeviceNamesFlow = connectedDevicesFlow.map { it.map { it.devicename } }

    internal suspend fun ensureStarted() {
        btScanner.ensureStarted()
    }

    internal suspend fun getMyDeviceName() = btScanner.getMyDeviceName()

    internal suspend fun listenForConnectionRequests(onNewConnection: suspend (deviceAddress: String) -> Unit) {
        btScanner.listenForClientConnectionRequest {
            val deviceAddress = it.remoteDevice.address
            addConnection(socket = it, deviceAddress = deviceAddress)
            btScanner.refreshPairedDevices()
            onNewConnection(deviceAddress)
        }
    }

    /**
     * Returns true if the connection was established and false otherwise
     */
    internal suspend fun connectToAsClient(deviceAddress: String): Boolean =
        withContext(dispatcherManager.io) {
            val socket = btScanner.connectToAsClient(deviceAddress)
            if (socket != null) {
                addConnection(socket = socket, deviceAddress = deviceAddress)
                btScanner.refreshPairedDevices()
            }
            socket != null
        }

    internal suspend fun disconnect(deviceAddress: String) = withContext(dispatcherManager.io) {
        connections[deviceAddress]?.disconnect()
    }

    internal suspend fun disconnectAll() = withContext(dispatcherManager.io) {
        connections.values.forEach { it.disconnect() }
    }

    internal suspend fun sendMessage(message: String, receiverDevicesAddresses: List<String>) {
        withContext(dispatcherManager.io) {
            Log.v("CommunicationManager", "sendMessage $receiverDevicesAddresses $message")

            receiverDevicesAddresses.mapNotNull { connections[it] }.forEach { connection ->
                connection.sendMessage(message)
            }
        }
    }

    internal suspend fun sendFile(file: File, receiverDeviceAddress: String) {
        connections[receiverDeviceAddress]!!.sendFile(file)
    }

    private fun addConnection(socket: BluetoothSocket, deviceAddress: String) {
        applicationScope.launch(dispatcherManager.io) {
            val connection = BtConnection(
                socket = socket,
                applicationScope = applicationScope,
                dispatcherManager = dispatcherManager,
                fileManager = fileManager,
                messageManager = messageManager,
            )

            updateConnectionsMutex.withLock {
                connections[deviceAddress] = connection
                _connectionState.value = connections.values.toSet()
            }

            connection.messageFlow
                .onEach {
                    val message = BtMessageWithAddress(message = it, deviceAddress = deviceAddress)
                    _messagesChannel.send(message)
                }
                .flowOn(dispatcherManager.io)
                .launchIn(this)

            connection.state
                .onEach { state ->
                    if (state is BtConnection.State.Disconnected) {
                        //TODO: making sure there is enough time to handle remaining messages; refactor
                        delay(100)
                        updateConnectionsMutex.withLock {
                            connections.remove(deviceAddress)
                            _connectionState.value = connections.values.toSet()
                        }
                        this.cancel()
                    }

                }
                .flowOn(dispatcherManager.io)
                .launchIn(this)

            connection.fileDownloadedEventFlow
                .onEach { event ->
                    _fileDownloadedEventChannel.emit(event)
                }
                .flowOn(dispatcherManager.io)
                .launchIn(this)
        }
    }

}
