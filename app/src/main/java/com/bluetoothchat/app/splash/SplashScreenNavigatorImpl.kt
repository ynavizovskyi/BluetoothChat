package com.bluetoothchat.app.splash

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.app.navigation.BaseNavigator
import com.bluetoothchat.app.navigation.NavRoutes
import com.bluetoothchat.app.navigation.ScreenTransition
import com.bluetoothchat.core.analytics.consts.SOURCE_SPLASH_SCREEN
import com.bluetoothchat.feature.profile.ProfileInputParams
import com.bluetoothchat.feature.profile.ProfileLaunchMode

val LocalSplashScreenNavigator =
    compositionLocalOf<SplashScreenNavigatorImpl> { error("LocalSplashScreenNavigator not provided") }

class SplashScreenNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack) {

    fun navigateToMain() {
        backStack.add(NavRoutes.Main(screenTransition = ScreenTransition.NONE))
            .apply { backStack.remove(NavRoutes.SplashScreen) }
    }

    fun navigateToMyProfile() {
        backStack.add(
            NavRoutes.Profile(
                ProfileInputParams(
                    mode = ProfileLaunchMode.Me(isInitialSetUp = true),
                    source = SOURCE_SPLASH_SCREEN,
                )
            )
        ).apply { backStack.remove(NavRoutes.SplashScreen) }
    }

}
