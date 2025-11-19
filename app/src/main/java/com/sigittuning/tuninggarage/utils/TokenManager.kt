package com.sigittuning.tuninggarage.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // Guardar token y datos del usuario
    suspend fun saveToken(token: String, userId: Int, name: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId.toString()
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Obtener token
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Obtener información del usuario
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    // Cerrar sesión (limpiar datos)
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Verificar si hay sesión activa
    suspend fun isLoggedIn(): Boolean {
        var hasToken = false
        context.dataStore.data.map { preferences ->
            hasToken = preferences[TOKEN_KEY] != null
        }
        return hasToken
    }
}