package com.example.aimoodjournal.presentation.ui.nux.setup

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aimoodjournal.R
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetupScreen(
    onNext: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    val infinite = rememberInfiniteTransition(label = "rings")
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val userName = state.userName
    val firstName = userName.split(" ").first()
    val view = LocalView.current
    
    val systemUiController = rememberSystemUiController()
    val setupScreenColor = Color(0xFF9FB26A)
    
    // Handle system bars color
    DisposableEffect(Unit) {
        systemUiController.setSystemBarsColor(
            color = setupScreenColor,
            darkIcons = false
        )
        onDispose {
            // Reset to default theme colors when leaving the screen
            systemUiController.setSystemBarsColor(
                color = Color(0xFF2F1C19),
                darkIcons = false
            )
        }
    }

    // Text animation state
    var currentTextIndex by remember { mutableStateOf(0) }
    val texts = remember { 
        listOf(
            "Hello, ${firstName}!",
            "Setting up your account",
            "One moment..."
        )
    }
    
    // Track if we've reached the last text
    var hasReachedLastText by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            for (i in 0 until texts.lastIndex) {
                delay(3000)
                currentTextIndex = i + 1
            }
            // Mark that we've reached the last text
            hasReachedLastText = true
        }
    }

    // Navigate to home after reaching last text
    LaunchedEffect(hasReachedLastText) {
        if (hasReachedLastText) {
            delay(3000) // Wait for the last text to be visible
            delay(350) // Additional small delay for better UX
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onNext()
        }
    }

    val orbiterIcons = listOf(
        R.drawable.heart_ic,
        R.drawable.book_ic,
        R.drawable.brain_ic,
        R.drawable.chat_bubble_ic,
        R.drawable.flower_ic,
        R.drawable.sleep_ic,
        R.drawable.smiley_ic,
    )

    /* ---- shared animations ------------------------------------------ */
    val ringProgress = listOf(
        infinite.animateFloat(0f, 1f,
            infiniteRepeatable(tween(6000, easing = LinearEasing))),
        infinite.animateFloat(0f, 1f,
            infiniteRepeatable(tween(6000, 2000, easing = LinearEasing))),
        infinite.animateFloat(0f, 1f,
            infiniteRepeatable(tween(6000, 4000, easing = LinearEasing)))
    )
    val orbitAngles = listOf(
        infinite.animateFloat(0f, 360f,
            infiniteRepeatable(tween(8000, easing = LinearEasing))),
        infinite.animateFloat(0f, 360f,
            infiniteRepeatable(tween(10000, easing = LinearEasing))),
        infinite.animateFloat(0f, 360f,
            infiniteRepeatable(tween(12000, easing = LinearEasing)))
    )

    val iconSize = 24.dp
    val iconSizePx = with(density) { iconSize.toPx() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = setupScreenColor
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF9FB26A)),          // screen background
            contentAlignment = Alignment.Center
        ) {
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val maxHeightPx = with(density) { maxHeight.toPx() }
            val centrePx = Offset(maxWidthPx / 2, maxHeightPx / 2)
            val startGapDp  = 200.dp
            val minRadiusPx = with(density) { startGapDp.toPx() }
            val maxRadiusPx = minOf(maxWidthPx, maxHeightPx) * 1.4f / 2f
            val strokePx    = with(density) { 1.dp.toPx() }

            /* ------------- Canvas for rings only ------------------------ */
            Canvas(Modifier.fillMaxSize()) {
                ringProgress.forEach { anim ->
                    val prog   = anim.value
                    val radius = minRadiusPx + (maxRadiusPx - minRadiusPx) * prog
                    val alpha  = 1f - prog
                    drawCircle(
                        color  = Color.White.copy(alpha = alpha * .8f),
                        radius = radius,
                        center = centrePx,
                        style  = Stroke(width = strokePx)
                    )
                }
            }

            val ringIcons = remember { List(3) { orbiterIcons.random() } }

            /* ------------- Orbiting icon layers ------------------------ */
            orbitAngles.forEachIndexed { idx, angleAnim ->

                val iconRes = ringIcons[idx]

                val prog     = ringProgress[idx].value
                val radius   = minRadiusPx + (maxRadiusPx - minRadiusPx) * prog
                val alpha    = 1f - prog
                val angleRad = Math.toRadians(angleAnim.value.toDouble())

                /* you can duplicate this block to put multiple icons per ring */
                val dx = (cos(angleRad) * radius).toFloat()
                val dy = (sin(angleRad) * radius).toFloat()

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(iconSize)
                        .offset {
                            IntOffset(
                                (dx - iconSizePx / 2).roundToInt(),
                                (dy - iconSizePx / 2).roundToInt()
                            )
                        }
                        .clip(CircleShape)               // keeps the white background round
                        .background(Color(0xFFE9EED9).copy(alpha = alpha))
                        .padding(4.dp)
                )
            }

            /* ------------- Centre stack --------------------------------- */
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingDots(
                    dotDiameter = 12.dp,
                    maxDotOffset = 20.dp,
                    cycleMillis = 1600
                )
                Spacer(Modifier.height(16.dp))
                
                // Animated text
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(280.dp), // Fixed width to prevent container resizing
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentTextIndex,
                        transitionSpec = {
                            val fadeSpec = tween<Float>(
                                durationMillis = 800,
                                easing = LinearEasing
                            )
                            fadeIn(fadeSpec) with fadeOut(fadeSpec)
                        }
                    ) { index ->
                        Text(
                            text = texts[index],
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth() // Ensure text uses full container width
                        )
                    }
                }
            }
        }
    }
}