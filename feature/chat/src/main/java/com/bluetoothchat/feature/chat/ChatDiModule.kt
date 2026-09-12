package com.bluetoothchat.feature.chat

import com.bluetoothchat.feature.chat.common.MessageActionHandlerDelegate
import com.bluetoothchat.feature.chat.image.saver.ImageSaver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val featureChatModule = module {
    singleOf(::ImageSaver)
    singleOf(::MessageActionHandlerDelegate)
}
