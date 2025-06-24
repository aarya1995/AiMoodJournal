package com.example.aimoodjournal.presentation.ui.journal_home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.R
import com.example.aimoodjournal.presentation.ui.shared.LoadingDots
import com.example.aimoodjournal.presentation.ui.shared.AiLoadingAnimationLarge
import java.time.LocalDate
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.platform.LocalView
import com.example.aimoodjournal.presentation.ui.journal_home.components.AIReportSection
import com.example.aimoodjournal.presentation.ui.journal_home.components.JournalEntrySection
import com.example.aimoodjournal.presentation.ui.journal_home.components.LlmConfigDialog

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun JournalHomeScreen(
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
    var showLlmConfigDialog by remember { mutableStateOf(false) }
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
                        text = "Please run the push_gemma.sh script to install the required model. Then restart the app.",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 0.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(
                            onClick = { showLlmConfigDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.llm_ic),
                                contentDescription = "ai settings icon",
                                modifier = Modifier.size(30.dp),
                                tint = Color(0xFFB1865E)
                            )
                        }
                    }
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
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !state.isSaving
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
                    com.example.aimoodjournal.presentation.ui.journal_home.components.DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        onDateSelected = { selectedDate ->
                            viewModel.navigateToDate(selectedDate)
                            showDatePicker = false
                        },
                        initialDate = state.currentDate
                    )
                }

                // LLM Config Dialog
                if (showLlmConfigDialog) {
                    LlmConfigDialog(
                        onDismissRequest = { showLlmConfigDialog = false },
                        onConfigSaved = { topK, topP, temperature, accelerator ->
                            viewModel.updateLlmConfig(topK, topP, temperature, accelerator)
                            showLlmConfigDialog = false
                        },
                        currentConfig = state.llmConfigOptions
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalEntryPage(
    date: LocalDate,
    viewModel: JournalHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val journal = viewModel.getJournalForDate(date)
    if (journal?.aiReport == null || state.isEditingJournal) {
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