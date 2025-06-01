package com.example.aimoodjournal.presentation.ui.nux.user_details

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
data class UserDetailsState(
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserDetailsState())
    val state: StateFlow<UserDetailsState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun saveName() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                userRepository.saveUserName(_state.value.name)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save name"
                    )
                }
            }
        }
    }
} 