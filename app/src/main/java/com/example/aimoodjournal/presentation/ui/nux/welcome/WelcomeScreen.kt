package com.example.aimoodjournal.presentation.ui.nux.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aimoodjournal.R
import com.example.aimoodjournal.presentation.ui.shared.PrimaryButton

@Composable
fun WelcomeScreen(
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Welcome Robot",
                modifier = Modifier.size(310.dp)
            )
            
            Text(
                text = "Your empathic AI journal, here to listen.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Lumen is your private AI journal, built for mental clarity and self-reflection. Your data is never shared and never leaves your device.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryButton(
                text = "Get Started",
                onClick = onNext,
                icon = Icons.AutoMirrored.Rounded.ArrowForward
            )
        }
    }
} 