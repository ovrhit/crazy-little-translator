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
    private val selectedPresetKey = stringPreferencesKey("selected_preset")

    val personaPrompt: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[personaPromptKey] ?: PersonaPresets.DEFAULT.prompt
    }

    val selectedPreset: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[selectedPresetKey] ?: PersonaPresets.DEFAULT.name
    }

    suspend fun savePersonaPrompt(prompt: String, presetName: String = "Custom") {
        context.dataStore.edit { preferences ->
            preferences[personaPromptKey] = prompt
            preferences[selectedPresetKey] = presetName
        }
    }
}

enum class PersonaPresets(val label: String, val prompt: String) {
    DEFAULT("기본 (존댓말)", "Translate in a polite and natural Korean tone (해요체)."),
    TSUNDERE("츤데레", "Translate like a tsundere anime character (rough but caring)."),
    SCHOLAR("선비", "Translate like a dignified Joseon scholar (archaic polite Korean)."),
    FRIENDLY("친근한 친구", "Translate like a close and casual friend (comfortable Banmal)."),
    CUSTOM("직접 입력", "")
}
