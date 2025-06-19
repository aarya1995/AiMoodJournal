package com.example.aimoodjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal): Long

    @Update
    suspend fun updateJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Query("SELECT * FROM journals ORDER BY timestamp DESC")
    fun getAllJournals(): Flow<List<Journal>>

    @Query("SELECT * FROM journals WHERE id = :id")
    suspend fun getJournalById(id: Long): Journal?

    @Query("SELECT * FROM journals WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getJournalsInTimeRange(startTime: Long, endTime: Long): Flow<List<Journal>>

    @Query("SELECT * FROM journals ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestJournal(): Journal?

    @Query("SELECT * FROM journals WHERE timestamp >= :startOfDay AND timestamp < :startOfNextDay ORDER BY timestamp DESC LIMIT 1")
    suspend fun getJournalForDate(startOfDay: Long, startOfNextDay: Long): Journal?
} 