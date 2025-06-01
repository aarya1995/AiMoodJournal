package com.example.aimoodjournal.presentation.ui.shared

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

val nuxEnterTransition = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { fullWidth -> fullWidth }
) + fadeIn(animationSpec = tween(300))

val nuxExitTransition = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { fullWidth -> -fullWidth }
) + fadeOut(animationSpec = tween(300))

val nuxPopEnterTransition = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { fullWidth -> -fullWidth }
) + fadeIn(animationSpec = tween(300))

val nuxPopExitTransition = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { fullWidth -> fullWidth }
) + fadeOut(animationSpec = tween(300)) 