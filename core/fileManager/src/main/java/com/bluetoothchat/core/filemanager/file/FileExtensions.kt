package com.bluetoothchat.core.filemanager.file

internal fun String.pathToFileFormat(): String {
    return this.substring(this.lastIndexOf("."))
}
