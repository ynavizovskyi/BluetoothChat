package com.bluetoothchat.core.filemanager

import com.bluetoothchat.core.filemanager.file.FileManager
import com.bluetoothchat.core.filemanager.image.ImageProcessor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreFileManagerModule = module {
    singleOf(::FileManager)
    singleOf(::ImageProcessor)
}
