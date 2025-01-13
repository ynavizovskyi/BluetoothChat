package com.bluetoothchat.core.session

import com.bluetoothchat.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface Session {

    fun init()

    suspend fun getAnalyticsUserId(): String

    suspend fun getUser(): User

    fun observeUser(): Flow<User>

    //We can't read our own device address so we have to rely on others sending it back
    suspend fun setUserAddressIfEmpty(address: String)

    suspend fun setDeviceNameIfChanged(deviceName: String?)

    suspend fun isCurrentUser(user: User): Boolean

    suspend fun isCurrentUser(deviceAddress: String): Boolean

}
