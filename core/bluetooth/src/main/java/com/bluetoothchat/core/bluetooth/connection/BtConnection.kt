package com.bluetoothchat.core.bluetooth.connection

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.bluetoothchat.core.bluetooth.message.MessageManager
import com.bluetoothchat.core.bluetooth.message.model.BtParseMessageResult
import com.bluetoothchat.core.bluetooth.message.model.MsgEndToken
import com.bluetoothchat.core.bluetooth.message.model.MsgStartToken
import com.bluetoothchat.core.bluetooth.message.model.Protocol
import com.bluetoothchat.core.bluetooth.message.model.entity.BtFileType
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.file.FileManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class BtConnection(
    private val socket: BluetoothSocket,
    private val applicationScope: ApplicationScope,
    private val dispatcherManager: DispatcherManager,
    private val fileManager: FileManager,
    private val messageManager: MessageManager,
) {
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val sendDataMutex = Mutex()
    private val receiveDataMutex = Mutex()

    private val address get() = socket.remoteDevice.address
    //Warning! This is actually nullable even though the compiler lets you treat as non nullable
    private val name: String? get() = socket.remoteDevice.name

    private val _messageChannel: Channel<BtParseMessageResult> = Channel(Channel.BUFFERED)
    val messageFlow = _messageChannel.receiveAsFlow()

    private val _fileDownloadedEventChannel: Channel<FileDownloadedEvent> = Channel(Channel.BUFFERED)
    val fileDownloadedEventFlow = _fileDownloadedEventChannel.receiveAsFlow()

    private val _state: MutableStateFlow<State> =
        MutableStateFlow(State.Listening(deviceAddress = address, devicename = name))
    val state: StateFlow<State> = _state

    init {
        try {
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            Log.v("BluetoothConnection", "Streams opened")

            readInput(socket.inputStream)
        } catch (e: Exception) {
            Log.v("BluetoothConnection", "Streams opening failed: $e")
        }
    }

    fun disconnect() {
        try {
            socket.close()
            Log.v("BluetoothConnection", "Socket closed")
        } catch (e: IOException) {
            Log.v("BluetoothConnection", "Socket closing failed")
        }
    }

    /**
     * Returns true if the message was sent and false otherwise
     */
    suspend fun sendMessage(message: String): Boolean {
        return sendDataMutex.withLock {
            try {
                val protocolMessage = "${MsgStartToken}${message}${MsgEndToken}"
                outputStream?.write(protocolMessage.toByteArray(Charsets.UTF_8))
                outputStream?.flush()
                true
            } catch (e: IOException) {
                Log.v("BluetoothConnection", "Message sending failed: $message $e")
                false
            }
        }
    }

    suspend fun sendFile(file: File) {
        Log.v("BluetoothConnection", "Send File: ${file.name}")
        sendDataMutex.withLock {
            if (!file.exists()) {
                Log.v("BluetoothConnection", "File does not exist: ${file.name}")
                return@withLock
            }
            val fileInputStream = FileInputStream(file)
            val bufferedOutputStream = outputStream!!
            try {
                var sentBytes: Long = 0
                var length: Int
                val buffer = ByteArray(bufferSize)

                length = fileInputStream.read(buffer)
                while (length > -1) {
                    if (length > 0) {
                        try {
                            bufferedOutputStream.write(buffer, 0, length)
                            bufferedOutputStream.flush()
                        } catch (e: IOException) {
                            Log.v("BluetoothConnection", "Send File failed: $e")
                            break
                        }
                        sentBytes += length.toLong()
                        Log.v("BluetoothConnection", "File sending, sent: $sentBytes")
                    }
                    length = fileInputStream.read(buffer)
                }
                Log.v("BluetoothConnection", "File sending finished")

            } catch (e: Exception) {
                Log.v("BluetoothConnection", "Send File failed: $e")
            } finally {
                fileInputStream.close()
            }
        }
    }

    private fun readInput(inputStream: InputStream) = applicationScope.launch(dispatcherManager.io) {
        receiveDataMutex.withLock {
            val buffer = ByteArray(bufferSize)
            //This is needed because we might get single message spread across multiple readings
            //Or multiple messages in one reading
            var accMessages = ""
            while (state.value is State.Listening) {
                try {
                    val bytesNumberRead = inputStream.read(buffer)
                    val readBytesString = String(buffer, 0, bytesNumberRead)
                    Log.v("BluetoothConnection", "readInput, readBytesString: $readBytesString")
                    accMessages += readBytesString

                    while (accMessages.contains(MsgStartToken) && accMessages.contains(MsgEndToken)) {
                        val messageStartIndex = accMessages.indexOf(MsgStartToken) + MsgStartToken.length
                        val messageEndIndex = accMessages.indexOf(MsgEndToken)

                        val message = accMessages.substring(startIndex = messageStartIndex, endIndex = messageEndIndex)
                        val parseResult = messageManager.parseMessage(message)
                        _messageChannel.send(parseResult)

                        //Clearing parsed message from accumulated string
                        accMessages = accMessages.substring(accMessages.indexOf(MsgEndToken) + MsgEndToken.length)

                        val startFIleBytes = if (accMessages.isNotEmpty()) {
                            val fileBytesStartIndex = readBytesString.indexOf(MsgEndToken) + MsgEndToken.length

                            Log.v(
                                "BluetoothConnection",
                                "startFIleBytes, fileBytesStartIndex: $fileBytesStartIndex, bytesNumberRead: $bytesNumberRead"
                            )
                            buffer.copyOfRange(fileBytesStartIndex, bytesNumberRead)
                        } else {
                            null
                        }

                        if (parseResult is BtParseMessageResult.Success && parseResult.message is Protocol.File.Response) {
                            readFile(
                                inputStream = inputStream,
                                startFileBytes = startFIleBytes,
                                fileType = parseResult.message.fileType,
                                fileName = parseResult.message.fileName,
                                fileSize = parseResult.message.fileSize,
                                chatId = parseResult.message.chatId ?: socket.remoteDevice.address,
                            )
                        }
                    }
                    //TODO: This does not get triggered when bluetooth is turned off during the exchange!! (Android 14; Android 10 seems to work fine)
                } catch (e: IOException) {
                    Log.v("BluetoothConnection", "Read input FAILED: $e")
                    _state.value = State.Disconnected(deviceAddress = address, devicename = name)
                    break
                }
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun readFile(
        inputStream: InputStream,
        startFileBytes: ByteArray?,
        fileType: BtFileType,
        fileName: String,
        fileSize: Long,
        chatId: String
    ) {
        val folderFile = when (fileType) {
            BtFileType.USER_FILE -> fileManager.getOrCreateUsersFolder()
            BtFileType.CHAT_FILE -> fileManager.getOrCreateChatFolder(chatId = chatId)
        }
        val file = File(folderFile, fileName)
        Log.v("BluetoothConnection", "Read file: ${file.name}")

        _state.value = State.FileTransfer.Receiving(
            deviceAddress = address,
            devicename = name,
            chatId = chatId,
            fileName = fileName,
            fileType = fileType,
            sizeBytes = fileSize,
            bytesTransferred = 0,
        )

        val fileOs = FileOutputStream(file)
        var totalBytesRead: Long = 0
        val buffer = ByteArray(bufferSize)

        startFileBytes?.let {
            Log.v("BluetoothConnection", "Read file, startFileBytes size: ${it.size}")
            val startFileBytesSize = it.size.toLong()
            while (totalBytesRead < startFileBytesSize) {
                val remainingSize = startFileBytesSize - totalBytesRead
                val byteCount = Math.min(remainingSize, bufferSize.toLong()).toInt()
                fileOs.write(it, totalBytesRead.toInt(), byteCount)
                fileOs.flush()
                totalBytesRead += byteCount
            }
            Log.v("BluetoothConnection", "Read file, startFileBytes bytesRead: ${totalBytesRead}")
        }

        while (totalBytesRead < fileSize) {
            val remainingSize = fileSize - totalBytesRead
            val bytesToReadCount = Math.min(remainingSize, bufferSize.toLong()).toInt()

            val bytesRead = inputStream.read(buffer, 0, bytesToReadCount)
            if (bytesRead > 0) {
                fileOs.write(buffer, 0, bytesRead)
                fileOs.flush()
                totalBytesRead += bytesRead.toLong()
                _state.value = State.FileTransfer.Receiving(
                    deviceAddress = address,
                    devicename = name,
                    chatId = chatId,
                    fileName = fileName,
                    fileType = fileType,
                    sizeBytes = fileSize,
                    bytesTransferred = totalBytesRead,
                )
                Log.v("BluetoothConnection", "File reading progress: $totalBytesRead")
            }
        }
        fileOs.close()
        Log.v("BluetoothConnection", "File reading finished, length: $totalBytesRead")

        _fileDownloadedEventChannel.send(FileDownloadedEvent(fileName = fileName, fileType = fileType, chatId = chatId))

        _state.value = State.Listening(deviceAddress = address, devicename = name)
    }

    sealed interface State {
        val deviceAddress: String
        val devicename: String?

        data class Disconnected(override val deviceAddress: String, override val devicename: String?) : State
        data class Listening(override val deviceAddress: String, override val devicename: String?) : State

        sealed interface FileTransfer : State {
            val chatId: String
            val fileName: String
            val fileType: BtFileType
            val sizeBytes: Long
            val bytesTransferred: Long

            data class Sending(
                override val deviceAddress: String,
                override val devicename: String?,
                override val chatId: String,
                override val fileName: String,
                override val fileType: BtFileType,
                override val sizeBytes: Long,
                override val bytesTransferred: Long,
            ) : FileTransfer

            data class Receiving(
                override val deviceAddress: String,
                override val devicename: String?,
                override val chatId: String,
                override val fileName: String,
                override val fileType: BtFileType,
                override val sizeBytes: Long,
                override val bytesTransferred: Long,
            ) : FileTransfer
        }
    }

    data class FileDownloadedEvent(val fileName: String, val fileType: BtFileType, val chatId: String)

    companion object {
        private const val bufferSize = 2048
    }
}
