package com.bluetoothchat.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> ContentCrossFade(
    targetState: T,
    modifier: Modifier = Modifier,
    contentKey: (targetState: T) -> Any?,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "ContentCrossFade",
    content: @Composable (T) -> Unit,
) {
    val transition = updateTransition(targetState, label)
    transition.Crossfade(
        modifier = modifier,
        animationSpec = animationSpec,
        contentKey = contentKey,
        content = content
    )
}
