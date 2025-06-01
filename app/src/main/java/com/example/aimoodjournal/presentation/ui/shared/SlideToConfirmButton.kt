package com.example.aimoodjournal.presentation.ui.shared

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.aimoodjournal.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SlideToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var width by remember { mutableStateOf(0f) }
    var dragOffset by remember { mutableStateOf(0f) }
    val view = LocalView.current
    
    val dragProgress = (dragOffset / width).coerceIn(0f, 1f)
    val thumbOffset by animateFloatAsState(
        targetValue = dragOffset,
        label = "thumbOffset"
    )
    
    val backgroundColor = Color(0xFF533630)
    val accentColor = Color(0xFF926247)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Background track
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(32.dp),
            color = backgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.alpha(1f - dragProgress)
                )
            }
        }

        // Draggable thumb
        Surface(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .fillMaxHeight()
                .aspectRatio(1f)
                .draggable(
                    enabled = enabled,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragOffset = (dragOffset + delta).coerceIn(0f, width)
                    },
                    onDragStopped = {
                        if (dragProgress > 0.9f) {
                            delay(400L)
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onConfirm()
                        }
                        dragOffset = 0f
                    }
                )
                .onGloballyPositioned {
                    width = it.parentLayoutCoordinates?.size?.width?.toFloat()?.minus(it.size.width) ?: 0f
                },
            shape = RoundedCornerShape(32.dp),
            color = accentColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.double_arrow_ic),
                    contentDescription = "Slide to confirm",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
} 