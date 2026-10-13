package com.bluetoothchat.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent

fun <T : Any> createSlideInEnterTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
    { screenEnterTransition() togetherWith screenExitTransition() }

fun <T : Any> createSlideInExitTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
    { screenPopEnterTransition() togetherWith screenPopExitTransition() }

fun <T : Any> createPredictiveSlideInExitTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform =
    { screenPopEnterTransition() togetherWith screenPopExitTransition() }

fun createNoAnimationMetadata(): Map<String, Any> =
    NavDisplay.transitionSpec { createNoAnimationTransition() } +
            NavDisplay.popTransitionSpec { createNoAnimationTransition() } +
            NavDisplay.predictivePopTransitionSpec { createNoAnimationTransition() }

private fun createNoAnimationTransition() = EnterTransition.None togetherWith ExitTransition.None
