package com.example.aimoodjournal.presentation.ui.journal_home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Immutable
@RequiresApi(Build.VERSION_CODES.O)
data class JournalHomeState(
    val isModelInstalled: Boolean = false,
    val output: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val currentPageIndex: Int = 10000 // Start at a large positive number to allow backwards navigation
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class JournalHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(JournalHomeState())
    val state: StateFlow<JournalHomeState> = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    companion object {
        const val PAGE_COUNT =
            20000 // Large enough to allow significant backwards/forwards navigation
        const val INITIAL_PAGE =
            10000 // Start in the middle to allow equal backwards/forwards navigation
    }

    init {
        checkModelInstallation()
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