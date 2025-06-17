package com.example.aimoodjournal.presentation.ui.entrypoint

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class MainState(
    val hasCompletedNux: Boolean? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadNuxState()
    }

    private fun loadNuxState() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                userRepository.isNuxCompleted().collect { hasCompleted ->
                    _state.update { it.copy(
                        hasCompletedNux = hasCompleted,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Failed to load NUX state",
                    isLoading = false
                ) }
            }
        }
    }
} 