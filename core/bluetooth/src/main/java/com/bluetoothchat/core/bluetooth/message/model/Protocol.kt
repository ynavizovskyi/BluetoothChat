package com.bluetoothchat.core.bluetooth.message.model

import com.bluetoothchat.core.bluetooth.message.model.entity.BtFileType
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChat
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatClientInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatHashInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtGroupChatInfo
import com.bluetoothchat.core.bluetooth.message.model.entity.BtMessage
import com.bluetoothchat.core.bluetooth.message.model.entity.BtUser
import kotlinx.serialization.Serializable

val MsgStartToken = "!!@@##"
val MsgEndToken = "$$%%^^"
val MsgComponentDivider = "$$$"
val ProtocolVersion = 1

sealed interface Protocol {

    sealed interface InitConnection : Protocol {
        @Serializable
        data class Request(
            //sending their address back so they can save it
            val receiverDeviceAddress: String,
            //Requesting up to date user information if changed
            val userHash: Int?,
            //Requesting up to date information about my group chats the user own if any changed
            val receiverAdminGroupChatsInfo: List<BtGroupChatHashInfo>,
            //Checking if they didn't delete any chats while not connected
            val receiverClientGroupChatIds: List<String>,
        ) : InitConnection {
            companion object {
                val type = "0_1"
            }
        }

        @Serializable
        data class Response(
            //sending their address back so they can save it
            val receiverDeviceAddress: String,
            val user: BtUser?,
            //Needed for localizing message time
            val hostTimestamp: Long,
            val privateChatExists: Boolean,
            val groupChatsInfo: List<BtGroupChatInfo>,
            //Returning info about whether the chat we are in on the host still exists on our side
            val btGroupChatClientInfo: List<BtGroupChatClientInfo>,
        ) : InitConnection {
            companion object {
                val type = "0_2"
            }
        }
    }

    sealed interface File : Protocol {
        @Serializable
        data class Request(
            //Null for private chats that don't have matching id on  both devices
            val chatId: String?,
            val fileType: BtFileType,
            val fileName: String,
        ) : File {
            companion object {
                val type = "0_3"
            }
        }

        @Serializable
        data class Response(
            //Null for private chats that don't have matching id on  both devices
            val chatId: String?,
            val fileType: BtFileType,
            val fileName: String,
            val fileSize: Long,
        ) : File {
            companion object {
                val type = "0_4"
            }
        }

    }

    sealed interface GroupChat : Protocol {
        @Serializable
        data class InviteToChatRequest(
            val chatId: String,
        ) : GroupChat {
            companion object {
                val type = "1_1"
            }
        }

        @Serializable
        data class InviteToChatResponse(
            val chatId: String,
            val user: BtUser,
            //For users that have chat history and need to catch up
            val lastMessageId: String?,
        ) : GroupChat {
            companion object {
                val type = "1_2"
            }
        }

        @Serializable
        data class ChatInitiationMessage(
            val hostTimestamp: Long,
            val chat: BtGroupChat,
            val messages: List<BtMessage>,
        ) : GroupChat {
            companion object {
                val type = "1_3"
            }
        }

        @Serializable
        data class HostChatMessage(
            val hostTimestamp: Long,
            val chatId: String,
            val chatHash: Int,
            val message: BtMessage,
        ) : GroupChat {
            companion object {
                val type = "1_4"
            }
        }

        @Serializable
        data class ClientChatMessage(
            val chatId: String,
            val userHash: Int,
            val message: BtMessage,
        ) : GroupChat {
            companion object {
                val type = "1_5"
            }
        }

        @Serializable
        data class ChatInfoRequest(
            val chatId: String,
        ) : GroupChat {
            companion object {
                val type = "1_6"
            }
        }

        //TODO: handle connected devices here
        @Serializable
        data class ChatInfoResponse(
            val chat: BtGroupChat,
        ) : GroupChat {
            companion object {
                val type = "1_7"
            }
        }

        @Serializable
        data class UserInfoRequest(
            //Not needed
            val address: String,
        ) : GroupChat {
            companion object {
                val type = "1_8"
            }
        }

        @Serializable
        data class UserInfoResponse(
            val user: BtUser,
        ) : GroupChat {
            companion object {
                val type = "1_9"
            }
        }

        @Serializable
        data class FileReady(
            val fileType: BtFileType,
            val chatId: String,
            val fileName: String,
        ) : GroupChat {
            companion object {
                val type = "1_10"
            }
        }

        @Serializable
        data class LeaveChatReqeust(
            val chatId: String,
        ) : GroupChat {
            companion object {
                val type = "1_11"
            }
        }
    }

    sealed interface PrivateChat : Protocol {
        @Serializable
        data class InviteToChatRequest(
            val dummy: String,
        ) : PrivateChat {
            companion object {
                val type = "2_1"
            }
        }

        @Serializable
        data class InviteToChatResponse(
            val user: BtUser,
        ) : PrivateChat {
            companion object {
                val type = "2_2"
            }
        }

        @Serializable
        data class ChatInitiationMessage(val user: BtUser) : PrivateChat {
            companion object {
                val type = "2_3"
            }
        }

        @Serializable
        data class ChatMessage(
            val message: BtMessage,
            val userHash: Int,
        ) : PrivateChat {
            companion object {
                val type = "2_4"
            }
        }

        @Serializable
        data class UserInfoRequest(
            val userDeviceAddress: String,
        ) : PrivateChat {
            companion object {
                val type = "2_5"
            }
        }

        @Serializable
        data class UserInfoResponse(
            val user: BtUser,
        ) : PrivateChat {
            companion object {
                val type = "2_6"
            }
        }
    }

}
