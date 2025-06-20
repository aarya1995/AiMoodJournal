package com.example.aimoodjournal.presentation.ui.journal_home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.aimoodjournal.R
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.presentation.ui.journal_home.JournalHomeState
import com.example.aimoodjournal.presentation.ui.journal_home.JournalHomeViewModel

private const val MIN_JOURNAL_TEXT_LENGTH = 40

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalEntrySection(
    modifier: Modifier = Modifier,
    journal: JournalEntry?,
    state: JournalHomeState,
    viewModel: JournalHomeViewModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        TextField(
            value = state.currentJournalText,
            onValueChange = viewModel::onJournalTextChanged,
            placeholder = {
                Text(
                    text = "What's on your mind?",
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
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = false, // Allow multiple lines for journal entries
            enabled = !state.isSaving,
            minLines = 3,
            maxLines = 10
        )

        // Character count and minimum requirement messaging
        val currentLength = state.currentJournalText.length
        val isMinLengthMet = currentLength >= MIN_JOURNAL_TEXT_LENGTH

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentLength / $MIN_JOURNAL_TEXT_LENGTH characters",
                style = MaterialTheme.typography.bodySmall,
                color = if (isMinLengthMet) Color.White.copy(alpha = 0.7f) else Color(0xFF926247),
            )
        }

        // Spacer to push the button to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Analyze Button
        Button(
            onClick = viewModel::saveJournalEntry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF926247)
            ),
            enabled = state.currentJournalText.trim().isNotEmpty() &&
                    state.currentJournalText.length >= MIN_JOURNAL_TEXT_LENGTH &&
                    !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "Analyze",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ai_icon),
                    contentDescription = "AI Analysis",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 