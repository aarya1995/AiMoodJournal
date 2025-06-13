package com.example.aimoodjournal.presentation.ui.journal_home

import android.content.Context
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
import javax.inject.Inject

@Immutable
data class JournalHomeState(
    val isModelInstalled: Boolean = false,
    val output: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class JournalHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(JournalHomeState())
    val state: StateFlow<JournalHomeState> = _state.asStateFlow()

    init {
        checkModelInstallation()
    }

    private fun checkModelInstallation() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Check if model exists in app's files directory
                val modelDir = File(context.filesDir, "models")
                val modelFile = File(modelDir, "gemma-3n-E4B-it-int4.task")
                val isInstalled = modelFile.exists() && modelFile.length() > 0

                if (isInstalled) {
                    // Set the configuration options for the LLM Inference task
                    val taskOptions = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(File(context.filesDir, "models/gemma-3n-E4B-it-int4.task").absolutePath)
                        .setMaxTopK(64)
                        .build()

                    // Create an instance of the LLM Inference task
                    val llmInference = LlmInference.createFromOptions(context, taskOptions)
//                    val result = llmInference.generateResponse("hello how are you?")
//                    _state.update {
//                        it.copy(
//                            output = result,
//                            isLoading = false
//                        )
//                    }
                }
                
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