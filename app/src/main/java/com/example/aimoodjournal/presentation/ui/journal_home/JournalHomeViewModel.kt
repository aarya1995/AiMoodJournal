package com.example.aimoodjournal.presentation.ui.journal_home

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.domain.llmchat.LlmChatHelper
import com.example.aimoodjournal.domain.llmchat.LlmPerfMetrics
import com.example.aimoodjournal.domain.model.AIReport
import com.example.aimoodjournal.domain.model.Accelerator
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.domain.model.LlmConfigOptions
import com.example.aimoodjournal.domain.model.UserData
import com.example.aimoodjournal.domain.repository.JournalRepository
import com.example.aimoodjournal.domain.repository.UserRepository
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** CAUTION:
 * Location where model was downloaded after the push_gemma.sh script run. If the script
 * is modified then this path may need to be updated accordingly.
 */
const val MODEL_DOWNLOAD_PATH = "models/gemma-3n-E4B-it-int4.task"

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
    private val llmChatHelper: LlmChatHelper,
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
        initializeLlmChatHelper()
    }

    private fun initializeLlmChatHelper() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val configOptions = _state.value.llmConfigOptions
                llmChatHelper.initialize(
                    context,
                    llmConfigOptions = configOptions,
                )
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

        // Reinitialize the LLM chat helper with new config
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                llmChatHelper.initialize(context, newConfig)
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
                val promptContext = getPromptContext(userData)
                val input = promptContext + currentText

                var aiReport: AIReport? = null
                // collect metrics for LLM inference
                var timeToFirstToken = 0f
                var prefillSpeed = 0f
                var decodeSpeed = 0f
                var latencySecs = 0f

                withContext(Dispatchers.IO) {
                    var prefillTokens = llmChatHelper.getNumTokens(input)
                    prefillTokens += _state.value.images.size * 257

                    var firstRun = true
                    var firstTokenTs = 0L
                    var decodeTokens = 0
                    val start = System.currentTimeMillis()
                    val output = StringBuilder()

                    // run inference with suspendCoroutine to make it blocking
                    try {
                        suspendCoroutine<Unit> { continuation ->
                            llmChatHelper.runInference(
                                input,
                                images = _state.value.images,
                                resultListener = { partialResult, done ->
                                    output.append(partialResult)
                                    val curTs = System.currentTimeMillis()

                                    if (firstRun) {
                                        firstTokenTs = System.currentTimeMillis()
                                        timeToFirstToken = (firstTokenTs - start) / 1000f
                                        prefillSpeed = prefillTokens / timeToFirstToken
                                        firstRun = false
                                    } else {
                                        decodeTokens++
                                    }

                                    if (done) {
                                        latencySecs = (curTs - start).toFloat() / 1000f
                                        decodeSpeed =
                                            decodeTokens / ((curTs - firstTokenTs) / 1000f)
                                        if (decodeSpeed.isNaN()) {
                                            decodeSpeed = 0f
                                        }

                                        // Parse the AI response when done
                                        val finalResponse = output.toString()
                                        aiReport = parseAIResponse(finalResponse)

                                        // Resume the coroutine when inference is complete
                                        continuation.resume(Unit)
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("JournalHomeViewModel", "Error generating AI response", e)
                        return@withContext
                    }
                }

                // create model perf metrics
                val accelerator = _state.value.llmConfigOptions.accelerator
                val llmPerfMetrics = LlmPerfMetrics(
                    timeToFirstToken = timeToFirstToken,
                    prefillSpeed = prefillSpeed,
                    decodeSpeed = decodeSpeed,
                    latencySeconds = latencySecs,
                    accelerator = accelerator.name,
                )

                Log.d("JournalHomeViewModel", "AI Report: $aiReport")
                // Create journal entry with AI report
                if (aiReport == null) {
                    _state.update {
                        it.copy(
                            saveError = "AI report failed to generate.",
                        )
                    }
                }

                // handle saving journal entry + ai report to db
                // check if journal entry already exists for today and we are performing an update/upsert
                val existingJournal = _state.value.journalEntries[currentDate]
                val existingJournalId = existingJournal?.id

                val journal = JournalEntry(
                    id = existingJournalId,
                    timestamp = timestamp,
                    journalText = currentText,
                    imagePath = null, // TODO: Add image support later
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

    private fun parseAIResponse(response: String): AIReport? {
        return try {
            // Clean the response - remove any extra text before or after the JSON
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}') + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = response.substring(jsonStart, jsonEnd)
                Gson().fromJson(jsonString, AIReport::class.java)
            } else {
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e("JournalHomeViewModel", "Error parsing AI response JSON", e)
            null
        }
    }

    private fun createLLMInferenceTask(): LlmInference {
        val taskOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(
                File(
                    context.filesDir,
                    "models/gemma-3n-E4B-it-int4.task"
                ).absolutePath
            )
            .setMaxTopK(64)
            .setMaxTokens(5000)
            .build()

        // Create and return an instance of the LLM Inference task
        return LlmInference.createFromOptions(context, taskOptions)
    }

    private fun updateCurrentJournalText() {
        val currentDate = _state.value.currentDate
        val existingJournal = _state.value.journalEntries[currentDate]

        _state.update {
            it.copy(
                currentJournalText = existingJournal?.journalText ?: "",
                isEditingJournal = false,
            )
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