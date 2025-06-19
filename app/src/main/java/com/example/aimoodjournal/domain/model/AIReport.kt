package com.example.aimoodjournal.domain.model

data class AIReport(
    val journalTitle: String,
    val journalSummary: String,
    val mood: List<String>,
    val emotion: String,
    val trend: Float? = null,
    val emoji: String? = null,
)
