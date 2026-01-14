package com.bluetoothchat.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset

private const val exitScreenTransitionFraction = 8

fun <T> tween(
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing
): TweenSpec<T> = TweenSpec(durationMillis, delayMillis, easing)

fun AnimatedContentTransitionScope<*>.screenEnterTransition(): EnterTransition {
    return slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween())
}

fun AnimatedContentTransitionScope<*>.screenExitTransition(): ExitTransition {
    return slideOut(
        animationSpec = tween(),
        targetOffset = { size ->
            IntOffset(x = -size.width / exitScreenTransitionFraction, y = 0)
        },
    )
}

fun AnimatedContentTransitionScope<*>.screenPopEnterTransition(): EnterTransition {
    return slideIn(
        animationSpec = tween(),
        initialOffset = { size ->
            IntOffset(x = -size.width / exitScreenTransitionFraction, y = 0)
        },
    )
}

fun AnimatedContentTransitionScope<*>.screenPopExitTransition(): ExitTransition {
    return slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween())
}

