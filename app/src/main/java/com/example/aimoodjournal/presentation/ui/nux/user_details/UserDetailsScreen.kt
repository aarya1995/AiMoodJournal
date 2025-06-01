package com.example.aimoodjournal.presentation.ui.nux.user_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.presentation.ui.shared.PrimaryButton

@Composable
fun UserDetailsScreen(
    onNext: () -> Unit,
    viewModel: UserDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.error) {
        state.error?.let {
            // Handle error (could show a snackbar or dialog)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = 24.dp,
                start = 24.dp,
                end = 24.dp,
                top = 64.dp,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            TextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                placeholder = {
                    Text(
                        text = "What is your name?",
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
                enabled = !state.isLoading
            )

            PrimaryButton(
                text = "Continue",
                onClick = {
                    viewModel.saveName()
                    onNext()
                },
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                enabled = state.name.isNotBlank() && !state.isLoading
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
} 