package com.bluetoothchat.core.bluetooth.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.bluetoothchat.core.bluetooth.BuildConfig
import com.bluetoothchat.core.bluetooth.util.isPhone
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BtScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val applicationScope: ApplicationScope,
    private val dispatcherManager: DispatcherManager,
) {

    var started = false

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val discoveredDevices = mutableMapOf<String, BluetoothDevice>()

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.isPhone()) {
                        discoveredDevices[device.address] = device
                    }
                    internalStateFlow.value = internalStateFlow.value.copy(
                        foundDevices = discoveredDevices.values.toList(),
                        pairedDevices = getPairedDevices().toList(),
                    )
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    internalStateFlow.value = internalStateFlow.value.copy(
                        isScanning = false,
                        foundDevices = discoveredDevices.values.toList(),
                        pairedDevices = getPairedDevices()
                    )
                    context.unregisterReceiver(this)
                }
            }
        }
    }

    private val discoverableStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)

            if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                internalStateFlow.value = internalStateFlow.value.copy(isDiscoverable = true)
            } else {
                internalStateFlow.value = internalStateFlow.value.copy(isDiscoverable = false)
            }
        }
    }

    private val internalStateFlow: MutableStateFlow<BtScannerStateInternal> =
        MutableStateFlow(
            BtScannerStateInternal(
                isDiscoverable = false,
                isScanning = false,
                foundDevices = emptyList(),
                pairedDevices = emptyList(),
            )
        )

    val stateFlow: StateFlow<BtScannerState> =
        internalStateFlow.map { state ->
            BtScannerState(
                isDiscoverable = state.isDiscoverable,
                isScanning = state.isScanning,
                foundDevices = state.foundDevices.map { device ->
                    BtDevice(address = device.address, name = device.name)
                },
                pairedDevices = state.pairedDevices.map { device ->
                    BtDevice(address = device.address, name = device.name)
                }
            )
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BtScannerState(false, false, emptyList(), emptyList()),
        )

    fun ensureStarted() {
        if (!started && isBluetoothEnabled()) {
            started = true
            internalStateFlow.value = internalStateFlow.value.copy(pairedDevices = getPairedDevices())
        }
    }

    fun refreshPairedDevices() {
        internalStateFlow.value = internalStateFlow.value.copy(pairedDevices = getPairedDevices())

        applicationScope.launch(dispatcherManager.default) {
            //The device is not returned in the bonded devices list; needs some delay *facepalm*
            delay(1000)
            internalStateFlow.value = internalStateFlow.value.copy(pairedDevices = getPairedDevices())
        }
    }

    fun isDevicePaired(address: String) = internalStateFlow.value.pairedDevices.any { it.address == address }

    fun isBluetoothAvailable(): Boolean = adapter != null

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled ?: false

    fun getMyDeviceName(): String? = adapter?.name

    fun getPairedDevices(): List<BluetoothDevice> = adapter?.bondedDevices?.toList() ?: emptyList()

    fun startScanning() {
        if (adapter == null) return

        ensureStarted()

        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }

        internalStateFlow.value = internalStateFlow.value.copy(isScanning = true)
        adapter.startDiscovery()
        context.registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        //Not working when launched from init() for some reason
        context.registerReceiver(discoverableStateReceiver, IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
    }

    fun stopScanning() {
        if (adapter != null && adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(discoveryReceiver)
            context.unregisterReceiver(discoverableStateReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    internal suspend fun connectToAsClient(deviceAddress: String): BluetoothSocket? {
        ensureStarted()

        val bluetoothDevice = internalStateFlow.value.pairedDevices.firstOrNull { it.address == deviceAddress }
            ?: internalStateFlow.value.foundDevices.firstOrNull { it.address == deviceAddress }
            ?: error("Device $deviceAddress not found")

        var socket: BluetoothSocket? = null
        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(appUuid)
            socket?.connect()
            Log.v("BluetoothScanner", "try connect SUCCESS: $deviceAddress")
        } catch (e: IOException) {
            Log.v("BluetoothScanner", "try connect CATCH ${e}")
            socket?.tryClose()
            socket = null
        }
        return socket
    }

    internal suspend fun listenForClientConnectionRequest(listener: suspend (BluetoothSocket) -> Unit) {
        withContext(Dispatchers.IO) {
            var socket: BluetoothServerSocket? = null
            try {
                val windowManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                socket = windowManager.adapter
                    ?.listenUsingRfcommWithServiceRecord("blAppName", appUuid)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            while (true) {
                try {
                    socket?.accept()?.let { socket ->
                        listener(socket)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private fun BluetoothSocket.tryClose() {
        try {
            this.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val appUuid = UUID.fromString(BuildConfig.APP_UUID)
    }
}
