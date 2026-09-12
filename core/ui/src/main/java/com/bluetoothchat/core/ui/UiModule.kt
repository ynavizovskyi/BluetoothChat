package com.bluetoothchat.core.ui

import com.bluetoothchat.core.ui.delegate.ChatActionHandlerDelegate
import com.bluetoothchat.core.ui.model.mapper.ViewBtDeviceMapper
import com.bluetoothchat.core.ui.model.mapper.ViewMessageActionsMapper
import com.bluetoothchat.core.ui.model.mapper.ViewMessageContentMapper
import com.bluetoothchat.core.ui.model.mapper.ViewMessageMapper
import com.bluetoothchat.core.ui.model.mapper.ViewUserMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewChatMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewGroupChatActionMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewGroupChatMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewPrivateChatActionMapper
import com.bluetoothchat.core.ui.model.mapper.chat.ViewPrivateChatMapper
import com.bluetoothchat.core.ui.util.TimeFormatter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreUiModule = module {
    singleOf(::TimeFormatter)

    singleOf(::ViewBtDeviceMapper)
    singleOf(::ViewUserMapper)
    singleOf(::ViewMessageActionsMapper)
    singleOf(::ViewMessageContentMapper)
    singleOf(::ViewMessageMapper)
    singleOf(::ViewChatMapper)
    singleOf(::ViewPrivateChatActionMapper)
    singleOf(::ViewPrivateChatMapper)
    singleOf(::ViewGroupChatActionMapper)
    singleOf(::ViewGroupChatMapper)

    factoryOf(::ChatActionHandlerDelegate)
}
