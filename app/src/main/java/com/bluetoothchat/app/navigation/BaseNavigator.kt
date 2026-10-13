package com.bluetoothchat.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.bluetoothchat.core.ui.navigation.Navigator

open class BaseNavigator(
    protected val backStack: NavBackStack<NavKey>,
) : Navigator {

    override fun navigateBack() {
        //The last entry should not be removed manually
        //Closing app is handled by navigation back click handler
        //when there is only one entry left
        //May crash without this check
        //Can't use removeLast():
        //https://www.reddit.com/r/androiddev/comments/1gspjrs/dont_use_kotlins_removefirst_and_removelast_when/
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

}
