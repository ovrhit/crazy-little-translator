package com.example.crazytranslator.repository

import android.content.Context
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.flow.collect

class TranslationRepository(private val context: Context) {

    // The ML Kit GenAI "Prompt" API (on-device Gemini Nano via AICore).
    // The default client is sufficient; this beta does not expose temperature /
    // maxOutputTokens through GenerationConfig/ModelConfig yet.
    private val model = Generation.getClient()

    suspend fun translateText(text: String, personaPrompt: String, screenContext: String = ""): String {
        if (text.isBlank()) return ""

        // checkStatus() is a suspend fun returning an @FeatureStatus Int.
        val status = model.checkStatus()
        if (status != FeatureStatus.AVAILABLE) {
            if (status == FeatureStatus.DOWNLOADABLE || status == FeatureStatus.DOWNLOADING) {
                // Drive the download flow to completion, then fall through to generate.
                return try {
                    model.download().collect { }
                    if (model.checkStatus() == FeatureStatus.AVAILABLE) {
                        generate(text, personaPrompt, screenContext)
                    } else {
                        "Downloading AI Model..."
                    }
                } catch (e: Exception) {
                    "Error: model download failed (${e.message})"
                }
            }
            return "On-device AI not available (Status: $status)"
        }

        return generate(text, personaPrompt, screenContext)
    }

    private suspend fun generate(text: String, personaPrompt: String, screenContext: String): String {
        // Sophisticated prompt for Gemini Nano using few-shot style and clear instructions
        val prompt = """
            [System Task]
            Translate the 'Target' into Korean naturally.

            [Instruction]
            1. Identify the speaker's tone from 'Target' (e.g., Japanese '~ze', '~wa', '~desu' or English slang vs formal).
            2. Use 'Screen Context' to find character names or relationship roles.
            3. Combine this with the user's preference: $personaPrompt.
            4. If the speaker sounds informal, use Korean 'Banmal'. If formal, use 'Haeyo-che' or 'Hapsyo-che'.
            5. Keep character-specific unique endings if possible.

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
            response.candidates.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /** Returns an @FeatureStatus Int (see [FeatureStatus]). */
    suspend fun checkModelStatus(): Int {
        return model.checkStatus()
    }
}
