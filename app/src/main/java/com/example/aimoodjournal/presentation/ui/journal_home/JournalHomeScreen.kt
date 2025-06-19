package com.example.aimoodjournal.presentation.ui.journal_home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots
import com.example.aimoodjournal.presentation.ui.shared.AiLoadingAnimationLarge
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.LocalDate
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.example.aimoodjournal.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalHomeScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = JournalHomeViewModel.INITIAL_PAGE,
        pageCount = { JournalHomeViewModel.PAGE_COUNT }
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var isProgrammaticNavigation by remember { mutableStateOf(false) }

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
                            pageIndex = pageIndex,
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
    pageIndex: Int,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val journal = viewModel.getJournalForDate(date)

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
            if (aiReport.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI Report",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = aiReport,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show save error if any
        state.saveError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
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
                Icon(
                    painter = painterResource(id = R.drawable.ai_icon),
                    contentDescription = "AI Analysis",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
} 