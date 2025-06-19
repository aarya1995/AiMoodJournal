package com.example.aimoodjournal.domain.model

import com.example.aimoodjournal.data.dao.Journal
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class JournalEntry(
    val id: Long? = null,
    val timestamp: Long,
    val journalText: String,
    val imagePath: String? = null,
    val aiReport: AIReport? = null
)

fun Journal.toJournalEntry(): JournalEntry {
    val aiReport = try {
        if (aiReport.isNotEmpty()) {
            Gson().fromJson(aiReport, AIReport::class.java)
        } else {
            null
        }
    } catch (e: JsonSyntaxException) {
        // If JSON parsing fails, return null for aiReport
        null
    }
    
    return JournalEntry(
        id = id,
        timestamp = timestamp,
        journalText = journalText,
        imagePath = imagePath,
        aiReport = aiReport
    )
}

fun JournalEntry.toJournal(): Journal {
    val aiReportJson = aiReport?.let { report ->
        try {
            Gson().toJson(report)
        } catch (e: Exception) {
            // If JSON serialization fails, return empty string
            ""
        }
    } ?: ""
    
    return Journal(
        id = id ?: 0L,
        timestamp = timestamp,
        journalText = journalText,
        imagePath = imagePath,
        aiReport = aiReportJson
    )
}
