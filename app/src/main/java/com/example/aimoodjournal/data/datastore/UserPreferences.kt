package com.example.aimoodjournal.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aimoodjournal.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
private const val HAS_COMPLETED_NUX = "has_completed_nux"
private const val USER_NAME = "user_name"
private const val USER_GENDER = "user_gender"
private const val USER_DOB = "user_dob"

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val hasCompletedNuxKey = booleanPreferencesKey(HAS_COMPLETED_NUX)
    private val userNameKey = stringPreferencesKey(USER_NAME)
    private val userGenderKey = stringPreferencesKey(USER_GENDER)
    private val userDobKey = stringPreferencesKey(USER_DOB)

    suspend fun completeNux() {
        context.dataStore.edit { preferences ->
            preferences[hasCompletedNuxKey] = true
        }
    }

    fun isNuxCompleted(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[hasCompletedNuxKey] ?: false
            }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[userNameKey] = name
        }
    }

    fun getUserName(): Flow<String?> {
        return context.dataStore.data
            .map { preferences ->
                preferences[userNameKey]
            }
    }

    suspend fun saveUserGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[userGenderKey] = gender
        }
    }

    suspend fun saveUserDateOfBirth(dob: String) {
        context.dataStore.edit { preferences ->
            preferences[userDobKey] = dob
        }
    }

    fun getUserData(): Flow<UserData> {
        return context.dataStore.data
            .map { preferences ->
                val userName = preferences[userNameKey]
                val userGender = preferences[userGenderKey]
                val userDob = preferences[userDobKey]

                UserData(
                    name = userName ?: "",
                    gender = userGender ?: "",
                    dateOfBirth = userDob ?: ""
                )
            }
    }
}