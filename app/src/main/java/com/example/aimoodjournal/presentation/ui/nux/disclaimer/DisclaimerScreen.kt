package com.example.aimoodjournal.presentation.ui.nux.disclaimer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.R
import com.example.aimoodjournal.presentation.ui.shared.SlideToConfirmButton

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DisclaimerScreen(
    onNext: () -> Unit,
    viewModel: DisclaimerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Precautions & Limitations",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 40.dp),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Not a Mental Health Advice Card
        DisclaimerCard(
            icon = R.drawable.stethoscope_ic,
            title = "Not Medical Advice",
            description = "This app is not a substitute for medical advice - it's intended for informational and wellness purposes only.",
            iconTint = Color(0xFF84CC16) // Light green color from screenshot
        )

        // Information is Limited Card
        DisclaimerCard(
            icon = R.drawable.llm_ic,
            title = "Information is Limited",
            description = "Our models are trained on a pre-existing data set, so they may not know recent info.",
            iconTint = Color(0xFFFACC15) // Yellow color from screenshot
        )

        // Data Accuracy Card
        DisclaimerCard(
            icon = R.drawable.target_ic,
            title = "Data Accuracy",
            description = "Any assessments depend on the accuracy of your input and the available models.",
            iconTint = Color(0xFF8B5CF6)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SlideToConfirmButton(
            text = "I understand the precautions",
            onConfirm = {
                viewModel.completeNux(onSuccess = onNext)
            },
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DisclaimerCard(
    icon: Int,
    title: String,
    description: String,
    iconTint: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF533630)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFFFFF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD6D3D1).copy(alpha = 0.8f)
                )
            }
        }
    }
} 