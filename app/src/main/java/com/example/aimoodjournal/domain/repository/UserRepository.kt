package com.example.aimoodjournal.domain.repository

import com.example.aimoodjournal.domain.model.UserBiometrics
import com.example.aimoodjournal.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUserName(name: String)
    suspend fun saveUserBiometrics(biometrics: UserBiometrics)
    fun getUserName(): Flow<String?>
    suspend fun completeNux()
    fun isNuxCompleted(): Flow<Boolean>
    fun getUserData(): Flow<UserData>
} 