package com.bluetoothchat.core.filemanager.file

sealed interface FileState {

    val fileName: String
    val fileSizeBytes: Long

    data class Missing(override val fileName: String, override val fileSizeBytes: Long) : FileState

    data class Downloaded(override val fileName: String, val path: String, override val fileSizeBytes: Long) : FileState

    data class Downloading(
        override val fileName: String,
        override val fileSizeBytes: Long,
        val bytesDownloaded: Long
    ) : FileState

}
