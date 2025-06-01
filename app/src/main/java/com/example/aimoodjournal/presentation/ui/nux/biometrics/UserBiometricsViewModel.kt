package com.example.aimoodjournal.presentation.ui.nux.biometrics

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimoodjournal.domain.model.UserBiometrics
import com.example.aimoodjournal.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class UserBiometricsState(
    val gender: String = "",
    val dateOfBirth: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserBiometricsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserBiometricsState())
    val state: StateFlow<UserBiometricsState> = _state.asStateFlow()

    fun updateGender(gender: String) {
        _state.value = _state.value.copy(gender = gender)
    }

    fun updateDateOfBirth(dob: String) {
        _state.value = _state.value.copy(dateOfBirth = dob)
    }

    fun saveBiometrics(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                val biometrics = UserBiometrics(
                    gender = _state.value.gender,
                    dateOfBirth = _state.value.dateOfBirth
                )
                userRepository.saveUserBiometrics(biometrics)
                
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while saving user biometrics"
                )
            }
        }
    }
} 