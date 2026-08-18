package com.example.crazytranslator.repository

import android.content.Context
import com.example.crazytranslator.Secret
import com.google.ai.client.generativeai.GenerativeModel

class TranslationRepository(private val context: Context) {

    private val apiKey = Secret.GEMINI_API_KEY.trim()
    val isConfigured: Boolean = apiKey.isNotEmpty()

    // Cloud Gemini. Change the model name here if needed.
    private val model: GenerativeModel? =
        if (isConfigured) GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey) else null

    suspend fun translateText(text: String, personaPrompt: String, screenContext: String = ""): String {
        if (text.isBlank()) return ""
        val model = this.model ?: return NOT_CONFIGURED

        val prompt = """
            [System Task]
            Translate the 'Target' into Korean naturally.

            [Instruction]
            1. Identify the speaker's tone from 'Target' (e.g., Japanese '~ze', '~wa', '~desu' or English slang vs formal).
            2. Use 'Screen Context' to find character names or relationship roles.
            3. Combine this with the user's preference: $personaPrompt.
            4. If the speaker sounds informal, use Korean 'Banmal'. If formal, use 'Haeyo-che' or 'Hapsyo-che'.
            5. Keep character-specific unique endings if possible.
            6. Output ONLY the Korean translation, with no explanation or quotes.

            [Context]
            $screenContext

            [Examples]
            Target: "俺の勝ちだぜ！" -> Result: "내가 이겼다고!" (Tough/Informal)
            Target: "お待ちしておりました。" -> Result: "기다리고 있었습니다." (Formal/Polite)
            Target: "何してるの？" -> Result: "뭐 하고 있어?" (Friendly/Informal)

            [Target]
            $text

            [Result]
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text?.trim() ?: ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /** Human-readable status shown on the main screen. */
    fun checkModelStatus(): String {
        return if (isConfigured) "Cloud Gemini Ready ($MODEL_NAME)"
        else "API 키 미설정 (Secret.kt)"
    }

    companion object {
        private const val MODEL_NAME = "gemini-flash-lite-latest"
        const val NOT_CONFIGURED = "API 키가 설정되지 않았습니다 (Secret.kt)"
    }
}
