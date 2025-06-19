package com.example.aimoodjournal.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journals")
data class Journal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val journalText: String,
    val imagePath: String? = null,
    val aiReport: String // JSON string containing the AI report card
) 