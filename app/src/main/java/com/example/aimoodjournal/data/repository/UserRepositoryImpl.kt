package com.example.aimoodjournal.data.repository

import com.example.aimoodjournal.data.datastore.UserPreferences
import com.example.aimoodjournal.domain.model.UserBiometrics
import com.example.aimoodjournal.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : UserRepository {
    
    override suspend fun saveUserName(name: String) {
        userPreferences.saveUserName(name)
    }

    override suspend fun saveUserBiometrics(biometrics: UserBiometrics) {
        userPreferences.saveUserGender(biometrics.gender)
        userPreferences.saveUserDateOfBirth(biometrics.dateOfBirth)
    }

    override fun getUserName(): Flow<String?> {
        return userPreferences.getUserName()
    }

    override suspend fun completeNux() {
        userPreferences.completeNux()
    }

    override fun isNuxCompleted(): Flow<Boolean> {
        return userPreferences.isNuxCompleted()
    }
} 