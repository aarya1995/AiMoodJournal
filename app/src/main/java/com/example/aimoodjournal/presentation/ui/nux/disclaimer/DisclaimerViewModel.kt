package com.example.aimoodjournal.presentation.ui.nux.disclaimer

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
data class DisclaimerState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DisclaimerState())
    val state: StateFlow<DisclaimerState> = _state.asStateFlow()

    fun completeNux(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                userRepository.completeNux()
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to complete setup"
                    )
                }
            }
        }
    }
} 