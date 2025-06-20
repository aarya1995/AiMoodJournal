package com.example.aimoodjournal.domain.llmchat

data class LlmPerfMetrics(
    val timeToFirstToken: Float = 0f,
    val prefillSpeed: Float = 0f,
    val decodeSpeed: Float = 0f,
    val latencyMs: Float = 0f,
)
