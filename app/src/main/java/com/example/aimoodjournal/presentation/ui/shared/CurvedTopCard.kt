package com.example.aimoodjournal.presentation.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun CurvedTopCard(
    modifier: Modifier = Modifier,
    curveHeight: Float = 40f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CurvedTopShape(curveHeight))
            .background(Color(0xFF533630))
    ) {
        content()
    }
}

fun CurvedTopShape(curveHeight: Float = 40f): Shape = GenericShape { size, _ ->
    moveTo(0f, curveHeight)
    quadraticBezierTo(
        size.width / 2, -curveHeight,
        size.width, curveHeight
    )
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}