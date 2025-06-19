package com.example.aimoodjournal.data.repository

import com.example.aimoodjournal.data.dao.Journal
import com.example.aimoodjournal.data.dao.JournalDao
import com.example.aimoodjournal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {
    
    override suspend fun insertJournal(journal: Journal): Long {
        return journalDao.insertJournal(journal)
    }

    override suspend fun updateJournal(journal: Journal) {
        journalDao.updateJournal(journal)
    }

    override suspend fun deleteJournal(journal: Journal) {
        journalDao.deleteJournal(journal)
    }

    override fun getAllJournals(): Flow<List<Journal>> {
        return journalDao.getAllJournals()
    }

    override suspend fun getJournalById(id: Long): Journal? {
        return journalDao.getJournalById(id)
    }

    override fun getJournalsInTimeRange(startTime: Long, endTime: Long): Flow<List<Journal>> {
        return journalDao.getJournalsInTimeRange(startTime, endTime)
    }

    override suspend fun getLatestJournal(): Journal? {
        return journalDao.getLatestJournal()
    }

    override suspend fun getJournalForDate(startOfDay: Long, startOfNextDay: Long): Journal? {
        return journalDao.getJournalForDate(startOfDay, startOfNextDay)
    }
} 