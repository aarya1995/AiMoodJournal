package com.example.aimoodjournal.presentation.ui.journal_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots

@Composable
fun JournalHomeScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                LoadingDots()
            }
            state.error != null -> {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            !state.isModelInstalled -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Model Not Installed",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Please run the push_gemma.sh script to install the required model.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = {
                            Text(
                                text = "What is on your mind?",
                                style = MaterialTheme.typography.headlineLarge,
                                fontStyle = FontStyle.Italic,
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFFFFFFFF),
                            selectionColors = TextSelectionColors(
                                handleColor = Color.Transparent,
                                backgroundColor = Color.Transparent
                            )
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = true,
                    )
                }
            }
        }
    }
} 