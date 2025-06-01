package com.example.aimoodjournal.presentation.ui.nux.biometrics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aimoodjournal.R
import com.example.aimoodjournal.presentation.ui.shared.PrimaryButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBiometricsScreen(
    onNext: () -> Unit,
    viewModel: UserBiometricsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isGenderDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isFormValid = remember(state.gender, state.dateOfBirth) {
        state.gender.isNotEmpty() && state.dateOfBirth.isNotEmpty()
    }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd/yyyy") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Input
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF2F1C19),
                titleContentColor = Color(0xFFD6D3D1),
                headlineContentColor = Color(0xFFD6D3D1),
                weekdayContentColor = Color(0xFFD6D3D1),
                subheadContentColor = Color(0xFFD6D3D1),
                yearContentColor = Color(0xFFD6D3D1),
                currentYearContentColor = Color(0xFFD6D3D1),
                selectedYearContentColor = Color(0xFFD6D3D1),
                selectedDayContainerColor = Color(0xFF926247),
                todayDateBorderColor = Color(0xFF926247),
                dayContentColor = Color(0xFFD6D3D1),
                selectedDayContentColor = Color(0xFFD6D3D1),
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            viewModel.updateDateOfBirth(dateFormatter.format(localDate))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFFD6D3D1))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel", color = Color(0xFFD6D3D1))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Please fill out the following details",
            modifier = Modifier.padding(top = 40.dp),
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // Gender Field
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.bodyMedium,
            )
            
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { isGenderDropdownExpanded = true }
                    }
            ) {
                OutlinedTextField(
                    value = state.gender,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2F1C19),
                        unfocusedContainerColor = Color(0xFF2F1C19),
                        disabledContainerColor = Color(0xFF2F1C19),
                        disabledTextColor = Color(0xFFD6D3D1),
                        focusedTextColor = Color(0xFFD6D3D1),
                        unfocusedTextColor = Color(0xFFD6D3D1),
                        focusedBorderColor = Color(0xFF44403C),
                        unfocusedBorderColor = Color(0xFF44403C)
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.gender_ic),
                            contentDescription = "Gender",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.White
                        )
                    }
                )

                DropdownMenu(
                    expanded = isGenderDropdownExpanded,
                    onDismissRequest = { isGenderDropdownExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFF2F1C19))
                        .padding(horizontal = 8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    listOf("Male", "Female", "Trans Male", "Trans Female", "Non-Binary", "Other").forEach { gender ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    gender,
                                    color = Color(0xFFD6D3D1),
                                    style = MaterialTheme.typography.bodyMedium,
                                ) 
                            },
                            onClick = {
                                viewModel.updateGender(gender)
                                isGenderDropdownExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Date of Birth Field
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Date of Birth",
                style = MaterialTheme.typography.bodyMedium,
            )
            
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { showDatePicker = true }
                    }
            ) {
                OutlinedTextField(
                    value = state.dateOfBirth,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2F1C19),
                        unfocusedContainerColor = Color(0xFF2F1C19),
                        disabledContainerColor = Color(0xFF2F1C19),
                        disabledTextColor = Color(0xFFD6D3D1),
                        focusedTextColor = Color(0xFFD6D3D1),
                        unfocusedTextColor = Color(0xFFD6D3D1),
                        focusedBorderColor = Color(0xFF44403C),
                        unfocusedBorderColor = Color(0xFF44403C),
                    ),
                    placeholder = {
                        Text("MM/DD/YYYY", color = Color.Gray)
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar_ic),
                            contentDescription = "Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        PrimaryButton(
            text = "Continue",
            onClick = {
                viewModel.saveBiometrics(onSuccess = onNext)
            },
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            enabled = isFormValid && !state.isLoading,
        )
    }
}