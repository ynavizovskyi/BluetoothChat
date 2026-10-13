package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.feature.profile.ProfileNavigator

class ProfileNavigatorImpl(backStack: NavBackStack<NavKey>) : BaseNavigator(backStack), ProfileNavigator {

    override fun navigateToMain() {
        backStack.add(NavRoutes.Main(screenTransition = ScreenTransition.DEFAULT))
        backStack.clearUpTo<NavRoutes.Profile>(keepTopElement = false)
    }
}
