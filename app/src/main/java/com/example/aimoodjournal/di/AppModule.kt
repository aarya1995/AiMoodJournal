package com.example.aimoodjournal.di

import android.content.Context
import com.example.aimoodjournal.data.datastore.UserPreferences
import com.example.aimoodjournal.data.repository.UserRepositoryImpl
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
} 