package com.bluetoothchat.core.ui.model

import com.bluetoothchat.core.domain.model.Picture
import com.bluetoothchat.core.filemanager.file.FileState

fun FileState.toPictureDomain() = Picture(id = fileName, sizeBytes = fileSizeBytes)
