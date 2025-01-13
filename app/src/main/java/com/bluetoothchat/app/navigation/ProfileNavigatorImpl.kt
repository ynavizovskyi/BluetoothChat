package com.bluetoothchat.app.navigation

import androidx.navigation.NavController
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import com.bluetoothchat.feature.profile.ProfileNavigator
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class ProfileNavigatorImpl(navController: NavController, dialogResultRecipient: OpenResultRecipient<DialogResult>) :
    BaseNavigator(navController, dialogResultRecipient), ProfileNavigator {

    override fun navigateToMain() {
        navController.navigate(
            direction = MainScreenDestination(),
            navOptionsBuilder = {
                popUpTo(
                    route = ProfileScreenDestination.route,
                    popUpToBuilder = { inclusive = true }
                )
            },
        )
    }
}

