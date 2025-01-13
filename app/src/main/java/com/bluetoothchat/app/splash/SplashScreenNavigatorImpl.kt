package com.bluetoothchat.app.splash

import androidx.navigation.NavController
import com.bluetoothchat.app.splash.destinations.SplashScreenDestination
import com.bluetoothchat.core.analytics.consts.SOURCE_SPLASH_SCREEN
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.navigation.BaseNavigator
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.OpenResultRecipient

class SplashScreenNavigatorImpl(
    navController: NavController,
    dialogResultRecipient: OpenResultRecipient<DialogResult>,
) : BaseNavigator(navController, dialogResultRecipient) {

    fun navigateToMain() {
        navController.navigate(
            direction = MainScreenDestination(),
            navOptionsBuilder = {
                popUpTo(
                    route = SplashScreenDestination.route,
                    popUpToBuilder = { inclusive = true }
                )
            },
        )
    }

    fun navigateToMyProfile() {
        navController.navigate(
            direction = ProfileScreenDestination(
                ProfileInputParams(
                    ProfileLaunchMode.Me(isInitialSetUp = true),
                    source = SOURCE_SPLASH_SCREEN,
                )
            ),
            navOptionsBuilder = {
                popUpTo(
                    route = SplashScreenDestination.route,
                    popUpToBuilder = { inclusive = true }
                )
            },
        )
    }

}
