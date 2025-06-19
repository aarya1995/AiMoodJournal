package com.example.aimoodjournal.domain.repository

import com.example.aimoodjournal.data.dao.Journal
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    suspend fun insertJournal(journal: Journal): Long
    suspend fun updateJournal(journal: Journal)
    suspend fun deleteJournal(journal: Journal)
    fun getAllJournals(): Flow<List<Journal>>
    suspend fun getJournalById(id: Long): Journal?
    fun getJournalsInTimeRange(startTime: Long, endTime: Long): Flow<List<Journal>>
    suspend fun getLatestJournal(): Journal?
    suspend fun getJournalForDate(startOfDay: Long, startOfNextDay: Long): Journal?
} 