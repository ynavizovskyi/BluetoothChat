package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.feature.chat.group.GroupChatInputParams
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoInputParams
import com.bluetoothchat.feature.chat.image.ViewImageInputParams
import com.bluetoothchat.feature.chat.privat.PrivateChatInputParams
import com.bluetoothchat.feature.connect.group.addusers.AddUsersInputParams
import com.bluetoothchat.feature.connect.main.ConnectInputParams
import com.bluetoothchat.feature.profile.ProfileInputParams
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoutes : NavKey {

    @Serializable
    data object SplashScreen : NavRoutes

    @Serializable
    data class Main(val screenTransition: ScreenTransition) : NavRoutes

    @Serializable
    data class Connect(val params: ConnectInputParams) : NavRoutes

    @Serializable
    data object CreateGroup : NavRoutes

    @Serializable
    data class AddUsers(val params: AddUsersInputParams) : NavRoutes

    @Serializable
    data class PrivateChat(val params: PrivateChatInputParams) : NavRoutes

    @Serializable
    data class GroupChat(val params: GroupChatInputParams) : NavRoutes

    @Serializable
    data class GroupChatInfo(val params: GroupChatInfoInputParams) : NavRoutes

    @Serializable
    data class ViewImage(val params: ViewImageInputParams) : NavRoutes

    @Serializable
    data class Profile(val params: ProfileInputParams) : NavRoutes

    @Serializable
    data object Settings : NavRoutes

}

enum class ScreenTransition {
    DEFAULT, NONE
}
