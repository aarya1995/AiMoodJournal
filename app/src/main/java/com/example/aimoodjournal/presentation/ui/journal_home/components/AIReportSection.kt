package com.example.aimoodjournal.presentation.ui.journal_home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aimoodjournal.R
import com.example.aimoodjournal.domain.model.AIReport
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.presentation.ui.shared.CurvedTopCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.aimoodjournal.common.roundTo
import com.example.aimoodjournal.presentation.ui.journal_home.JournalHomeState
import com.example.aimoodjournal.presentation.ui.journal_home.JournalHomeViewModel

private const val JOURNAL_TEXT_PREVIEW_LENGTH = 150

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIReportSection(
    modifier: Modifier = Modifier,
    journal: JournalEntry,
    aiReport: AIReport,
    state: JournalHomeState,
    viewModel: JournalHomeViewModel,
) {
    val systemUiController = rememberSystemUiController()
    val curvedTopCardColor = Color(0xFF533630)
    var isJournalTextExpanded by remember { mutableStateOf(false) }

    // Change navigation bar color when AI Report is displayed
    DisposableEffect(Unit) {
        systemUiController.setNavigationBarColor(
            color = curvedTopCardColor,
            darkIcons = false
        )
        onDispose {
            // Reset to default theme colors when leaving the screen
            systemUiController.setNavigationBarColor(
                color = Color(0xFF2F1C19),
                darkIcons = false
            )
        }
    }

    val numWords = journal.journalText.trim().split("\\s+".toRegex()).size
    val emoji = when (aiReport.emoji) {
        "overjoyed" -> R.drawable.overjoyed_ic
        "happy" -> R.drawable.hapy_ic
        "neutral" -> R.drawable.neutral_ic
        "sad" -> R.drawable.sad_ic
        "depressed" -> R.drawable.depressed_ic
        else -> R.drawable.neutral_ic
    }

    // Get truncated or full text based on expansion state
    val displayText = if (isJournalTextExpanded) {
        journal.journalText
    } else {
        if (journal.journalText.length > JOURNAL_TEXT_PREVIEW_LENGTH) {
            journal.journalText.take(JOURNAL_TEXT_PREVIEW_LENGTH) + "..."
        } else {
            journal.journalText
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ai_icon),
                tint = Color(0xFF926247),
                contentDescription = "ai report icon",
                modifier = Modifier
                    .size(45.dp),
            )
            Text(
                text = aiReport.journalTitle,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "$numWords Total Words",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = aiReport.journalSummary,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
        CurvedTopCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 0.dp),
            curveHeight = 40f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Key Metrics",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.mood_ic),
                            contentDescription = "Mood Icon",
                            tint = Color(0xFFD6D3D1),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mood",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = aiReport.mood.joinToString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
                HorizontalDivider(
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.emotion_ic),
                            contentDescription = "Emotion",
                            tint = Color(0xFFD6D3D1),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Emotion",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = aiReport.emotion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
                if (journal.aiReport?.journalHighlights?.isEmpty() == false) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "Journal Highlights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = Color(0xFF2F1C19)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            journal.aiReport.journalHighlights.map { highlight ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.green_checkmark_ic),
                                        contentDescription = "green checkmark",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = highlight,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.book_ic),
                        contentDescription = "Book Icon",
                        tint = Color(0xFFA8A29E),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Journal Entry",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = Color(0xFF2F1C19)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Image(
                            painter = painterResource(emoji),
                            contentDescription = "Emoji",
                            modifier = Modifier.size(50.dp),
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .clickable(
                                        enabled = journal.journalText.length > JOURNAL_TEXT_PREVIEW_LENGTH
                                    ) {
                                        if (journal.journalText.length > JOURNAL_TEXT_PREVIEW_LENGTH) {
                                            isJournalTextExpanded = !isJournalTextExpanded
                                        }
                                    }
                            )
                            // Show expand/collapse indicator only if text is long enough
                            if (journal.journalText.length > JOURNAL_TEXT_PREVIEW_LENGTH) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = if (isJournalTextExpanded) "Tap to collapse" else "Tap to expand",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clickable {
                                            isJournalTextExpanded = !isJournalTextExpanded
                                        }
                                )
                            }
                        }
                    }
                }
                if (journal.llmPerfMetrics != null) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.stopwatch),
                            contentDescription = "stopwatch",
                            tint = Color(0xFFA8A29E),
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = "Stats on ${journal.llmPerfMetrics.accelerator}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = Color(0xFF2F1C19)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // time to first token
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "1st token",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${journal.llmPerfMetrics.timeToFirstToken.roundTo(2)}",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = "sec",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.LightGray,
                                    ),
                                )
                            }

                            // prefill speed
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Prefill speed",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${journal.llmPerfMetrics.prefillSpeed.roundTo(2)}",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = "tokens/s",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.LightGray,
                                    ),
                                )
                            }

                            // decode speed
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Decode speed",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${journal.llmPerfMetrics.decodeSpeed.roundTo(2)}",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = "tokens/s",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.LightGray,
                                    ),
                                )
                            }

                            // latency
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Latency",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${journal.llmPerfMetrics.latencySeconds.roundTo(2)}",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = "sec",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.LightGray,
                                    ),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { viewModel.editJournal() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF926247)
                    ),
                ) {
                    Text(
                        text = "Edit Journal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.edit_pencil_ic),
                        contentDescription = "AI Analysis",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
} 