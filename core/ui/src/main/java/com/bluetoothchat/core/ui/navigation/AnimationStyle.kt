package com.bluetoothchat.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

object StartScreenDestinationStyle : DestinationStyle.Animated {
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() = EnterTransition.None
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() = screenExitTransition()
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() = screenPopEnterTransition()
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() = screenPopExitTransition()
}
