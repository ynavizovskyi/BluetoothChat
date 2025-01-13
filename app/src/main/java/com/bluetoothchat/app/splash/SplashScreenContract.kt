package com.bluetoothchat.app.splash

import com.bluetoothchat.core.ui.mvi.contract.ViewOneTimeEvent
import com.bluetoothchat.core.ui.mvi.contract.ViewState

object SplashScreenState : ViewState

internal sealed interface SplashScreenEvent : ViewOneTimeEvent {

    object NavigateToProfileSetUpScreen : SplashScreenEvent

    object NavigateToMain : SplashScreenEvent

}
