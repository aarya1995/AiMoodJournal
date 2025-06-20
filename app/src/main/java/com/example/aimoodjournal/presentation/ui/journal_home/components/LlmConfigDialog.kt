package com.example.aimoodjournal.presentation.ui.journal_home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aimoodjournal.domain.model.Accelerator
import com.example.aimoodjournal.domain.model.LlmConfigOptions

@Composable
fun LlmConfigDialog(
    onDismissRequest: () -> Unit,
    onConfigSaved: (Int, Float, Float, Accelerator) -> Unit,
    currentConfig: LlmConfigOptions
) {
    var topKText by remember { mutableStateOf(currentConfig.topK.toString()) }
    var topPText by remember { mutableStateOf(currentConfig.topP.toString()) }
    var temperatureText by remember { mutableStateOf(currentConfig.temperature.toString()) }
    var selectedAccelerator by remember { mutableStateOf(currentConfig.accelerator) }

    var topKError by remember { mutableStateOf<String?>(null) }
    var topPError by remember { mutableStateOf<String?>(null) }
    var temperatureError by remember { mutableStateOf<String?>(null) }

    val accelerators = listOf(Accelerator.CPU, Accelerator.GPU)

    fun validateInputs(): Boolean {
        var isValid = true

        // Validate topK
        val topK = topKText.toIntOrNull()
        if (topK == null || topK < 0 || topK > 100) {
            topKError = "Must be 0-100"
            isValid = false
        } else {
            topKError = null
        }

        // Validate topP
        val topP = topPText.toFloatOrNull()
        if (topP == null || topP < 0f || topP > 1f) {
            topPError = "Must be 0.00-1.00"
            isValid = false
        } else {
            topPError = null
        }

        // Validate temperature
        val temperature = temperatureText.toFloatOrNull()
        if (temperature == null || temperature < 0f || temperature > 2f) {
            temperatureError = "Must be 0.00-2.00"
            isValid = false
        } else {
            temperatureError = null
        }

        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Model Configuration",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Model being used - hardcoded for now
                Text(
                    text = "Model: gemma-3n-E4B-it-int4",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                // Max Tokens
                Text(
                    text = "Max Tokens: ${currentConfig.maxTokens}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                // TopK Input
                OutlinedTextField(
                    value = topKText,
                    onValueChange = { topKText = it },
                    label = { Text("Top K (0-100)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    isError = topKError != null,
                    supportingText = topKError?.let { { Text(it, color = Color.Red) } }
                )

                // TopP Input
                OutlinedTextField(
                    value = topPText,
                    onValueChange = { topPText = it },
                    label = { Text("Top P (0.00-1.00)", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    isError = topPError != null,
                    supportingText = topPError?.let { { Text(it, color = Color.Red) } }
                )

                // Temperature Input
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { temperatureText = it },
                    label = {
                        Text(
                            "Temperature (0.00-2.00)",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    isError = temperatureError != null,
                    supportingText = temperatureError?.let { { Text(it, color = Color.Red) } }
                )

                // Accelerator Selection
                Column {
                    Text(
                        text = "Accelerator",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accelerators.forEach { accelerator ->
                            FilterChip(
                                onClick = { selectedAccelerator = accelerator },
                                label = { Text(accelerator.toString()) },
                                selected = selectedAccelerator == accelerator,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF926247),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateInputs()) {
                        onConfigSaved(
                            topKText.toInt(),
                            topPText.toFloat(),
                            temperatureText.toFloat(),
                            selectedAccelerator
                        )
                    }
                }
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF2F1C19),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
} 