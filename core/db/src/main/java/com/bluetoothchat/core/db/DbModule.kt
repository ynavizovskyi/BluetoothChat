package com.bluetoothchat.core.db

import com.bluetoothchat.core.db.datasource.ChatDataSource
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreDbModule = module {
    single<DatabaseManager> { RoomDatabaseManager.create(get()) }

    singleOf(::UserDataSource)
    singleOf(::MessageDataSource)
    singleOf(::ChatDataSource)
}
