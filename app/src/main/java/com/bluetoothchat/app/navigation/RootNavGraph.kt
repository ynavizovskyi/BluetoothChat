package com.bluetoothchat.app.navigation

import androidx.compose.ui.ExperimentalComposeUiApi
import com.bluetoothchat.app.splash.AppNavGraph
import com.bluetoothchat.core.ui.CoreUiNavGraph
import com.bluetoothchat.feature.chat.ChatNavGraph
import com.bluetoothchat.feature.main.MainNavGraph
import com.bluetoothchat.feature.connect.ConnectNavGraph
import com.bluetoothchat.feature.profile.ProfileNavGraph
import com.bluetoothchat.feature.settings.ui.SettingsNavGraph
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec

@ExperimentalComposeUiApi
object RootNavGraph : NavGraphSpec {

    override val route = "root"

    override val destinationsByRoute = emptyMap<String, DestinationSpec<*>>()

    override val startRoute = AppNavGraph

    override val nestedNavGraphs = listOf(
        AppNavGraph,
        CoreUiNavGraph,
        ChatNavGraph,
        MainNavGraph,
        ConnectNavGraph,
        ProfileNavGraph,
        SettingsNavGraph,
    )
}

