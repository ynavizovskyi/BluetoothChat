package com.bluetoothchat.core.bluetooth.message.model.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface BtGroupChatInfo {
    val chatId: String

    @Serializable
    @SerialName("groupChatInfoDeleted")
    data class Deleted(
        override val chatId: String,
    ) : BtGroupChatInfo

    @Serializable
    @SerialName("groupChatInfoExists")
    data class Exists(
        override val chatId: String,
        //Null if group info hash has not changed
        val chat: BtGroupChat?,
        val messages: List<BtMessage>,
    ) : BtGroupChatInfo

}
