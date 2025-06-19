package com.example.aimoodjournal.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Journal::class],
    version = 1,
    exportSchema = false
)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
} 