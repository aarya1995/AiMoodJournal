package com.example.aimoodjournal.data.repository

import com.example.aimoodjournal.data.dao.JournalDao
import com.example.aimoodjournal.domain.model.JournalEntry
import com.example.aimoodjournal.domain.model.toJournal
import com.example.aimoodjournal.domain.model.toJournalEntry
import com.example.aimoodjournal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {
    
    override suspend fun insertJournal(journal: JournalEntry): Long {
        return journalDao.insertJournal(journal.toJournal())
    }

    override suspend fun updateJournal(journal: JournalEntry) {
        journalDao.updateJournal(journal.toJournal())
    }

    override suspend fun deleteJournal(journal: JournalEntry) {
        journalDao.deleteJournal(journal.toJournal())
    }

    override fun getAllJournals(): Flow<List<JournalEntry>> {
        return journalDao.getAllJournals().map { journals ->
            journals.map { it.toJournalEntry() }
        }
    }

    override suspend fun getJournalById(id: Long): JournalEntry? {
        return journalDao.getJournalById(id)?.toJournalEntry()
    }

    override fun getJournalsInTimeRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>> {
        return journalDao.getJournalsInTimeRange(startTime, endTime).map { journals ->
            journals.map { it.toJournalEntry() }
        }
    }

    override suspend fun getLatestJournal(): JournalEntry? {
        return journalDao.getLatestJournal()?.toJournalEntry()
    }

    override suspend fun getJournalForDate(startOfDay: Long, startOfNextDay: Long): JournalEntry? {
        return journalDao.getJournalForDate(startOfDay, startOfNextDay)?.toJournalEntry()
    }
} 