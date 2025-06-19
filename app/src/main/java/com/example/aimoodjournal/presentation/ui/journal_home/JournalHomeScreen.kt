package com.example.aimoodjournal.presentation.ui.journal_home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots
import com.example.aimoodjournal.presentation.ui.shared.AiLoadingAnimationLarge
import java.time.LocalDate
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.aimoodjournal.R
import com.example.aimoodjournal.domain.model.AIReport
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.presentation.ui.shared.CurvedTopCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.platform.LocalView

private const val JOURNAL_TEXT_PREVIEW_LENGTH = 150

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun JournalHomeScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current
    val pagerState = rememberPagerState(
        initialPage = JournalHomeViewModel.INITIAL_PAGE,
        pageCount = { JournalHomeViewModel.PAGE_COUNT }
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var isProgrammaticNavigation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Track previous saving state for haptic feedback
    var wasSaving by remember { mutableStateOf(false) }

    // Blur system bars when loading
    DisposableEffect(state.isSaving) {
        if (state.isSaving) {
            // Blur the system bars during loading
            systemUiController.setSystemBarsColor(
                color = Color.Black.copy(alpha = 0.3f),
                darkIcons = false
            )
        } else {
            // Reset to default theme colors when not loading
            systemUiController.setSystemBarsColor(
                color = Color(0xFF2F1C19),
                darkIcons = false
            )
        }
        onDispose {
            // Ensure we reset when component is disposed
            systemUiController.setSystemBarsColor(
                color = Color(0xFF2F1C19),
                darkIcons = false
            )
        }
    }

    // Haptic feedback when saving completes
    LaunchedEffect(state.isSaving) {
        if (wasSaving && !state.isSaving) {
            // Transition from saving to not saving - play haptic feedback
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        }
        wasSaving = state.isSaving
    }

    // Show snack bar when saveError is set
    LaunchedEffect(state.saveError) {
        state.saveError?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    // Sync pager state with ViewModel state (only when arrow buttons are pressed or date picker is used)
    LaunchedEffect(state.currentPageIndex) {
        if (pagerState.currentPage != state.currentPageIndex && !isProgrammaticNavigation) {
            isProgrammaticNavigation = true
            pagerState.animateScrollToPage(state.currentPageIndex)
            isProgrammaticNavigation = false
        }
    }

    // Sync ViewModel state with pager state (only when swiping)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentPageIndex && !isProgrammaticNavigation) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Snackbar Host at the bottom with dark theme
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .padding(16.dp),
            snackbar = { data ->
                Snackbar(
                    containerColor = Color(0xFF2F1C19),
                    contentColor = Color.White
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        )

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
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (state.isSaving) {
                                Modifier.blur(10.dp)
                            } else {
                                Modifier
                            }
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Picker Header
                    DatePickerHeader(
                        currentDate = state.currentDate,
                        onPreviousDate = viewModel::navigateToPreviousDate,
                        onNextDate = viewModel::navigateToNextDate,
                        onDateClick = { showDatePicker = true },
                        getFormattedDate = viewModel::getFormattedDate
                    )

                    // Horizontal Pager for Journal Entries
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val pageDate = viewModel.getDateForPage(pageIndex)
                        JournalEntryPage(
                            date = pageDate,
                            viewModel = viewModel
                        )
                    }
                }

                // Date Picker Dialog
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        onDateSelected = { selectedDate ->
                            viewModel.navigateToDate(selectedDate)
                            showDatePicker = false
                        },
                        initialDate = state.currentDate
                    )
                }

                // AI Analysis Loading Overlay
                if (state.isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AiLoadingAnimationLarge()
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerHeader(
    currentDate: LocalDate,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onDateClick: () -> Unit,
    getFormattedDate: (LocalDate) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Date Button
        IconButton(
            onClick = onPreviousDate,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Date",
                tint = Color.White
            )
        }

        // Date Display (Clickable) with Down Caret
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onDateClick() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getFormattedDate(currentDate),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Open Date Picker",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Next Date Button
        IconButton(
            onClick = onNextDate,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Date",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convert milliseconds to LocalDate using UTC to avoid timezone issues
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = Color.White)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color(0xFF2F1C19),
            titleContentColor = Color.White,
            headlineContentColor = Color.White,
            weekdayContentColor = Color.White,
            subheadContentColor = Color.White,
            yearContentColor = Color.White,
            currentYearContentColor = Color.White,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = Color(0xFF926247),
            dayContentColor = Color.White,
            selectedDayContentColor = Color.White,
            selectedDayContainerColor = Color(0xFF926247),
            todayContentColor = Color(0xFF926247),
            todayDateBorderColor = Color(0xFF926247)
        )
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalEntryPage(
    date: LocalDate,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val journal = viewModel.getJournalForDate(date)
    if (journal?.aiReport == null) {
        JournalEntrySection(
            journal = journal,
            state = state,
            viewModel = viewModel,
        )
    } else {
        AIReportSection(
            journal = journal,
            aiReport = journal.aiReport,
            state = state,
            viewModel = viewModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { },
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

        // Show AI report if available
        journal?.aiReport?.let { aiReport ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AI Report",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = aiReport.journalTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = aiReport.journalSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = aiReport.mood.joinToString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = aiReport.emotion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = aiReport.emoji ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )
            aiReport.journalHighlights.map {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
            enabled = state.currentJournalText.trim().isNotEmpty() && !state.isSaving
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