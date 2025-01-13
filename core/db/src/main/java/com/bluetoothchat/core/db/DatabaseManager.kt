package com.bluetoothchat.core.db

import com.bluetoothchat.core.db.dao.GroupChatDao
import com.bluetoothchat.core.db.dao.GroupChatUserDao
import com.bluetoothchat.core.db.dao.MessageDao
import com.bluetoothchat.core.db.dao.PrivateChatDao
import com.bluetoothchat.core.db.dao.UserDao

interface DatabaseManager {

    fun userDao(): UserDao

    fun messageDao(): MessageDao

    fun privateChatDao(): PrivateChatDao

    fun groupChatDao(): GroupChatDao

    fun groupChatUserDao(): GroupChatUserDao

}
