package com.example.aimoodjournal.presentation.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.aimoodjournal.R

@Composable
fun AiLoadingAnimation(
    modifier: Modifier = Modifier,
    size: Int = 200,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ai_loading_animation)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying
    )
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier.size(size.dp)
        )
    }
}

@Composable
fun AiLoadingAnimationSmall(
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true
) {
    AiLoadingAnimation(
        modifier = modifier,
        size = 100,
        iterations = iterations,
        isPlaying = isPlaying
    )
}

@Composable
fun AiLoadingAnimationLarge(
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true
) {
    AiLoadingAnimation(
        modifier = modifier,
        size = 300,
        iterations = iterations,
        isPlaying = isPlaying
    )
}

@Composable
fun AiLoadingAnimationInButton(
    modifier: Modifier = Modifier,
    size: Int = 20
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ai_loading_animation)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = modifier.size(size.dp)
    )
} 