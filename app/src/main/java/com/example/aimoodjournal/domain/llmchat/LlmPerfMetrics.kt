package com.example.aimoodjournal.domain.llmchat

import com.example.aimoodjournal.domain.model.Accelerator

data class LlmPerfMetrics(
    val timeToFirstToken: Float = 0f,
    val prefillSpeed: Float = 0f,
    val decodeSpeed: Float = 0f,
    val latencySeconds: Float = 0f,
    val accelerator: String = Accelerator.CPU.name,
)
