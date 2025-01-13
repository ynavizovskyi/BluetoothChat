package com.bluetoothchat.app.splash

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle

@Destination(start = true, style = DestinationStyle.Animated.None::class)
@Composable
fun SplashScreen(navigator: SplashScreenNavigatorImpl) {
    val viewModel: SplashScreenViewModel = hiltViewModel()

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is SplashScreenEvent.NavigateToMain -> navigator.navigateToMain()
            is SplashScreenEvent.NavigateToProfileSetUpScreen -> navigator.navigateToMyProfile()
        }
    }

}
