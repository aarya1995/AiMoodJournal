package com.example.aimoodjournal.presentation.ui.journal_home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@Immutable
data class JournalHomeState(
    val isModelInstalled: Boolean = false,
    val output: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val currentPageIndex: Int = 10000, // Start at a large positive number to allow backwards navigation
    val journalEntries: Map<LocalDate, JournalEntry> = emptyMap(),
    val isJournalLoading: Boolean = false,
    val journalError: String? = null,
    val currentJournalText: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class JournalHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journalRepository: JournalRepository
) : ViewModel() {
    private val _state = MutableStateFlow(JournalHomeState())
    val state: StateFlow<JournalHomeState> = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    companion object {
        const val PAGE_COUNT =
            20000 // Large enough to allow significant backwards/forwards navigation
        const val INITIAL_PAGE =
            10000 // Start in the middle to allow equal backwards/forwards navigation
        const val FETCH_RANGE_YEARS = 2 // Fetch 2 years before and after current date
    }

    init {
        checkModelInstallation()
        loadJournalEntries()
    }

    fun getFormattedDate(date: LocalDate): String {
        val today = LocalDate.now()
        val formattedDate = date.format(dateFormatter)

        return if (date == today) {
            "$formattedDate (Today)"
        } else {
            formattedDate
        }
    }

    fun getDateForPage(pageIndex: Int): LocalDate {
        val today = LocalDate.now()
        val daysFromToday = pageIndex - INITIAL_PAGE
        return today.plusDays(daysFromToday.toLong())
    }

    fun getJournalForDate(date: LocalDate): JournalEntry? {
        return _state.value.journalEntries[date]
    }

    fun navigateToPreviousDate() {
        _state.update { currentState ->
            currentState.copy(
                currentDate = currentState.currentDate.minusDays(1),
                currentPageIndex = currentState.currentPageIndex - 1
            )
        }
    }

    fun navigateToNextDate() {
        _state.update { currentState ->
            currentState.copy(
                currentDate = currentState.currentDate.plusDays(1),
                currentPageIndex = currentState.currentPageIndex + 1
            )
        }
    }

    fun navigateToDate(date: LocalDate) {
        val today = LocalDate.now()
        val daysDifference = java.time.temporal.ChronoUnit.DAYS.between(today, date)
        val newPageIndex = INITIAL_PAGE + daysDifference.toInt()

        _state.update { currentState ->
            currentState.copy(
                currentDate = date,
                currentPageIndex = newPageIndex
            )
        }
    }

    fun onPageChanged(newPageIndex: Int) {
        val newDate = getDateForPage(newPageIndex)

        _state.update { currentState ->
            currentState.copy(
                currentDate = newDate,
                currentPageIndex = newPageIndex
            )
        }

        // Update the current journal text when page changes
        updateCurrentJournalText()
    }

    fun onJournalTextChanged(text: String) {
        _state.update { it.copy(currentJournalText = text) }
    }

    fun saveJournalEntry() {
        val currentText = _state.value.currentJournalText.trim()
        if (currentText.isEmpty()) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSaving = true, saveError = null) }

                val currentDate = _state.value.currentDate
                val timestamp =
                    currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val journal = JournalEntry(
                    timestamp = timestamp,
                    journalText = currentText,
                    imagePath = null, // TODO: Add image support later
                    aiReport = null, // TODO: Add AI analysis later
                )

                val journalId = journalRepository.insertJournal(journal)

                // Update the journal entries map with the new entry
                _state.update { currentState ->
                    currentState.copy(
                        journalEntries = currentState.journalEntries + (currentDate to journal.copy(
                            id = journalId
                        )),
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save journal entry"
                    )
                }
            }
        }
    }

    private fun updateCurrentJournalText() {
        val currentDate = _state.value.currentDate
        val existingJournal = _state.value.journalEntries[currentDate]

        _state.update {
            it.copy(currentJournalText = existingJournal?.journalText ?: "")
        }
    }

    private fun loadJournalEntries() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isJournalLoading = true, journalError = null) }

                val today = LocalDate.now()
                val startDate = today.minusYears(FETCH_RANGE_YEARS.toLong())
                val endDate = today.plusYears(FETCH_RANGE_YEARS.toLong())

                // Convert dates to timestamps for the repository query
                val startTimestamp =
                    startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTimestamp =
                    endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                journalRepository.getJournalsInTimeRange(startTimestamp, endTimestamp)
                    .collect { journals ->
                        // Convert list to map for O(1) lookup by date
                        val journalMap = journals.associate { journal ->
                            val journalDate = java.time.Instant.ofEpochMilli(journal.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            journalDate to journal
                        }

                        _state.update {
                            it.copy(
                                journalEntries = journalMap,
                                isJournalLoading = false
                            )
                        }

                        // Update the current journal text after entries are loaded
                        updateCurrentJournalText()
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isJournalLoading = false,
                        journalError = e.message ?: "Failed to load journal entries"
                    )
                }
            }
        }
    }

    private fun checkModelInstallation() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                // Check if model exists in app's files directory
                val modelDir = File(context.filesDir, "models")
                val modelFile = File(modelDir, "gemma-3n-E4B-it-int4.task")
                val isInstalled = modelFile.exists() && modelFile.length() > 0

                _state.update {
                    it.copy(
                        isModelInstalled = isInstalled,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to check model installation"
                    )
                }
            }
        }
    }
} 