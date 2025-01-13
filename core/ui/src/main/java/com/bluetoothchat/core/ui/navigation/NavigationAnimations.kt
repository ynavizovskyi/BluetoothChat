package com.bluetoothchat.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Stable

@Stable
fun <T> tween(
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing
): TweenSpec<T> = TweenSpec(durationMillis, delayMillis, easing)

fun AnimatedContentTransitionScope<*>.screenEnterTransition(): EnterTransition {
    return fadeIn(initialAlpha = 0.5f, animationSpec = tween()) +
            slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween())
}

fun AnimatedContentTransitionScope<*>.screenExitTransition(): ExitTransition {
    return fadeOut(targetAlpha = 0.999f, animationSpec = tween())
}

fun AnimatedContentTransitionScope<*>.screenPopEnterTransition(): EnterTransition {
    return fadeIn(initialAlpha = 0.999f, animationSpec = tween())
}

fun AnimatedContentTransitionScope<*>.screenPopExitTransition(): ExitTransition {
    return fadeOut(targetAlpha = 0.5f, animationSpec = tween()) +
            slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween())
}

