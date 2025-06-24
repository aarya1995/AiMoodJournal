package com.example.aimoodjournal.presentation.ui.journal_home

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.common.Constants.MODEL_DOWNLOAD_PATH
import com.example.aimoodjournal.domain.model.Accelerator
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.domain.model.LlmConfigOptions
import com.example.aimoodjournal.domain.model.UserData
import com.example.aimoodjournal.domain.repository.ImageRepository
import com.example.aimoodjournal.domain.repository.JournalRepository
import com.example.aimoodjournal.domain.repository.LlmRepository
import com.example.aimoodjournal.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val saveError: String? = null,
    val currentUserData: UserData? = null,
    val isEditingJournal: Boolean = false,
    val images: List<Bitmap> = listOf(),
    val selectedImagePath: String? = null,
    val selectedImageUri: Uri? = null,
    val llmConfigOptions: LlmConfigOptions = LlmConfigOptions(
        modelPath = MODEL_DOWNLOAD_PATH,
        topK = 64,
        maxTokens = 5000,
    ),
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class JournalHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journalRepository: JournalRepository,
    private val userRepository: UserRepository,
    private val llmRepository: LlmRepository,
    private val imageRepository: ImageRepository,
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
        const val MIN_JOURNAL_TEXT_LENGTH = 40 // Minimum characters required for journal entry
    }

    init {
        checkModelInstallation()
        loadJournalEntries()
        loadUserData()
        initializeLlmRepository()
    }

    private fun initializeLlmRepository() {
        viewModelScope.launch {
            try {
                val configOptions = _state.value.llmConfigOptions
                llmRepository.initialize(context, configOptions)
            } catch (e: Exception) {
                Log.e("JournalHomeViewModel", "Failed to initialize LLM repository", e)
                _state.update { it.copy(error = "Failed to initialize AI model") }
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getUserData().collectLatest { userData ->
                _state.update {
                    it.copy(currentUserData = userData)
                }
            }
        }
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

    fun editJournal() {
        _state.update { it.copy(isEditingJournal = true) }
    }

    fun updateLlmConfig(
        topK: Int,
        topP: Float,
        temperature: Float,
        accelerator: Accelerator
    ) {
        val newConfig = _state.value.llmConfigOptions.copy(
            topK = topK,
            topP = topP,
            temperature = temperature,
            accelerator = accelerator
        )

        _state.update { it.copy(llmConfigOptions = newConfig) }

        // Update the LLM repository with new config
        viewModelScope.launch {
            try {
                llmRepository.updateConfig(context, newConfig)
            } catch (e: Exception) {
                Log.e("JournalHomeViewModel", "Failed to update LLM config", e)
                _state.update { it.copy(error = "Failed to update AI model configuration") }
            }
        }
    }

    fun handleImageSelected(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val imagePath = imageRepository.saveImageToInternalStorage(context, uri)
                val imageUri = Uri.fromFile(File(imagePath))
                val bitmap = imageRepository.loadBitmapFromUri(context, imageUri)

                if (bitmap != null) {
                    _state.update {
                        it.copy(
                            selectedImagePath = imagePath,
                            selectedImageUri = imageUri,
                            images = listOf(bitmap)
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Failed to load selected image") }
                }
            } catch (e: Exception) {
                Log.e("JournalHomeViewModel", "Error handling image selection", e)
                _state.update { it.copy(error = "Failed to process selected image") }
            }
        }
    }

    fun saveJournalEntry() {
        val currentText = _state.value.currentJournalText.trim()
        val userData = _state.value.currentUserData
        if (currentText.isEmpty()) return
        if (currentText.length < MIN_JOURNAL_TEXT_LENGTH) return
        if (userData == null) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSaving = true, saveError = null) }

                val currentDate = _state.value.currentDate
                val timestamp =
                    currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Generate AI report using the repository
                val (aiReport, llmPerfMetrics) = llmRepository.generateAIReport(
                    journalText = currentText,
                    images = _state.value.images,
                    userData = userData,
                    llmConfigOptions = _state.value.llmConfigOptions
                )

                // Create journal entry with AI report
                val existingJournal = _state.value.journalEntries[currentDate]
                val existingJournalId = existingJournal?.id

                val imagePath = _state.value.selectedImagePath
                val journal = JournalEntry(
                    id = existingJournalId,
                    timestamp = timestamp,
                    journalText = currentText,
                    imagePath = imagePath,
                    aiReport = aiReport,
                    llmPerfMetrics = llmPerfMetrics,
                )

                // Save to database
                val journalId = journalRepository.insertJournal(journal)

                // Update the journal entries map with the new entry
                _state.update { currentState ->
                    currentState.copy(
                        journalEntries = currentState.journalEntries + (currentDate to journal.copy(
                            id = journalId
                        )),
                        isSaving = false,
                        isEditingJournal = false,
                    )
                }
            } catch (e: Exception) {
                Log.e("JournalHomeViewModel", "Error saving journal entry", e)
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save journal entry",
                        isEditingJournal = false,
                    )
                }
            }
        }
    }

    private fun updateCurrentJournalText() {
        viewModelScope.launch {
            val currentDate = _state.value.currentDate
            val existingJournal = _state.value.journalEntries[currentDate]

            var imagePath: String? = null
            var imageUri: Uri? = null
            var bitmap: Bitmap? = null

            if (existingJournal?.imagePath != null) {
                imagePath = existingJournal.imagePath
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    imageUri = Uri.fromFile(imageFile)
                    bitmap = imageRepository.loadBitmapFromUri(context, imageUri)
                }
            }

            _state.update {
                it.copy(
                    currentJournalText = existingJournal?.journalText ?: "",
                    isEditingJournal = false,
                    selectedImagePath = imagePath,
                    selectedImageUri = imageUri,
                    images = if (bitmap != null) listOf(bitmap) else emptyList()
                )
            }
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

                val isInstalled = llmRepository.isModelInstalled(context)

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