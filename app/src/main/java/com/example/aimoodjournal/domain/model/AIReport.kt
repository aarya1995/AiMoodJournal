package com.example.aimoodjournal.domain.model

import android.graphics.drawable.Drawable

data class AIReport(
    val journalTitle: String,
    val journalSummary: String,
    val mood: List<String>,
    val emotion: String,
    val trend: Float? = null,
    val emoji: String? = null,
)
