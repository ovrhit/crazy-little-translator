package com.example.crazytranslator.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    private val personaPromptKey = stringPreferencesKey("persona_prompt")

    val personaPrompt: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[personaPromptKey] ?: "Translate this text naturally in Korean."
    }

    suspend fun savePersonaPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[personaPromptKey] = prompt
        }
    }
}
