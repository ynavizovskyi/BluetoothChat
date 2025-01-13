package com.bluetoothchat.core.ui.model.mapper

import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.ui.model.ViewUserAction

interface ViewUserActionsMapper {

    suspend fun map(user: User, isConnected: Boolean): List<ViewUserAction>

}
