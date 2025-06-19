package com.example.aimoodjournal.di

import android.content.Context
import androidx.room.Room
import com.example.aimoodjournal.data.dao.JournalDatabase
import com.example.aimoodjournal.data.dao.JournalDao
import com.example.aimoodjournal.data.datastore.UserPreferences
import com.example.aimoodjournal.data.repository.JournalRepositoryImpl
import com.example.aimoodjournal.data.repository.UserRepositoryImpl
import com.example.aimoodjournal.domain.repository.JournalRepository
import com.example.aimoodjournal.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): UserPreferences = UserPreferences(context)

    @Provides
    @Singleton
    fun provideUserRepository(
        userPreferences: UserPreferences
    ): UserRepository = UserRepositoryImpl(userPreferences)

    @Provides
    @Singleton
    fun provideJournalDatabase(
        @ApplicationContext context: Context
    ): JournalDatabase = Room.databaseBuilder(
        context,
        JournalDatabase::class.java,
        "journal_database"
    ).build()

    @Provides
    @Singleton
    fun provideJournalDao(
        database: JournalDatabase
    ): JournalDao = database.journalDao()

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalDao: JournalDao
    ): JournalRepository = JournalRepositoryImpl(journalDao)
} 