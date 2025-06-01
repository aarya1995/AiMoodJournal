package com.example.aimoodjournal.presentation.ui.nux.setup

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SetupState(
    val userName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            try {
                userRepository.getUserName().collect { name ->
                    _state.update { it.copy(
                        userName = name ?: "",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Failed to load user name",
                    isLoading = false
                ) }
            }
        }
    }
} 