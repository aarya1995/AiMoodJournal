package com.example.aimoodjournal.domain.repository

import com.example.aimoodjournal.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    suspend fun insertJournal(journal: JournalEntry): Long
    suspend fun updateJournal(journal: JournalEntry)
    suspend fun deleteJournal(journal: JournalEntry)
    fun getAllJournals(): Flow<List<JournalEntry>>
    suspend fun getJournalById(id: Long): JournalEntry?
    fun getJournalsInTimeRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>>
    suspend fun getLatestJournal(): JournalEntry?
    suspend fun getJournalForDate(startOfDay: Long, startOfNextDay: Long): JournalEntry?
} 