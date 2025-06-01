package com.example.aimoodjournal.presentation.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier.size(48.dp),   // overall box
    dotColor: Color = Color.White,
    dotDiameter: Dp = 10.dp,                     // size of each dot
    maxDotOffset: Dp = 16.dp,                    // how far they fly out
    cycleMillis: Int = 1200                      // full “out-spin-in” cycle
) {
    // 0 → 1 progress for the whole cycle
    val progress by rememberInfiniteTransition(label = "spinner")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(cycleMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spinnerProgress"
        )

    val dotRadiusPx = with(LocalDensity.current) { (dotDiameter / 2).toPx() }
    val maxOffsetPx  = with(LocalDensity.current) { maxDotOffset.toPx() }

    // Saw-tooth curve: 0 → 1 (pull apart) then 1 → 0 (snap back)
    val radialProgress = if (progress <= .5f)  (progress / .5f)
    else                  ((1f - progress) / .5f)
    val currentOffset  = radialProgress * maxOffsetPx

    // Full 360° spin per cycle
    val rotationDeg = 360f * progress

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val center = Offset(size.width / 2, size.height / 2)

        repeat(4) { i ->                          // 0°, 90°, 180°, 270°
            val baseAngleRad = Math.toRadians((i * 90.0) + rotationDeg)
            val x = center.x + cos(baseAngleRad).toFloat() * currentOffset
            val y = center.y + sin(baseAngleRad).toFloat() * currentOffset
            drawCircle(color = dotColor, radius = dotRadiusPx, center = Offset(x, y))
        }
    }
}