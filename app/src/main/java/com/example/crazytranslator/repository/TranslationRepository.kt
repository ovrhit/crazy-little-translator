package com.example.crazytranslator.repository

import android.content.Context
import android.util.Log
import com.example.crazytranslator.Secret
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import org.json.JSONArray

class TranslationRepository(private val context: Context) {

    private val apiKey = Secret.GEMINI_API_KEY.trim()
    val isConfigured: Boolean = apiKey.isNotEmpty()

    // Cloud Gemini. Change the model name here if needed.
    private val model: GenerativeModel? =
        if (isConfigured) GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey) else null

    /**
     * Translates all [texts] in a SINGLE request (the free tier is ~15 req/min, so
     * one call per screen — not per text block — is essential). Returns a list aligned
     * to [texts]; failed/empty entries come back as an error string or "".
     */
    suspend fun translateBatch(
        texts: List<String>,
        personaPrompt: String,
        screenContext: String = ""
    ): List<String> {
        if (texts.isEmpty()) return emptyList()
        val model = this.model ?: return texts.map { NOT_CONFIGURED }

        val numbered = texts.mapIndexed { i, t -> "${i + 1}. ${t.replace("\n", " ")}" }.joinToString("\n")
        val prompt = """
            You translate on-screen text into natural Korean.
            Persona / tone preference: $personaPrompt
            Use informal Korean (반말) for casual speech, polite forms for formal speech,
            and keep character-specific speech endings when possible.
            Screen context (for names/relationships only, do NOT translate this): $screenContext

            Translate EACH numbered line below into Korean.
            Return ONLY a JSON array of strings — exactly ${texts.size} items, same order,
            no numbering, no code fences, no extra text.

            Lines:
            $numbered
        """.trimIndent()

        return try {
            Log.d("CLT-Trans", "batch request: ${texts.size} lines")
            val raw = model.generateContent(prompt).text?.trim().orEmpty()
            val parsed = parseJsonArray(raw)
            Log.d("CLT-Trans", "batch response: parsed=${parsed?.size} of ${texts.size}")
            if (parsed != null && parsed.size == texts.size) {
                parsed
            } else {
                // Best-effort: use what we can, leave the rest blank (keeps overlay clean).
                texts.indices.map { parsed?.getOrNull(it) ?: "" }
            }
        } catch (e: QuotaExceededException) {
            Log.e("CLT-Trans", "quota exceeded", e)
            texts.map { QUOTA_EXCEEDED }
        } catch (e: Exception) {
            Log.e("CLT-Trans", "batch failed", e)
            texts.map { "Error: ${e.message}" }
        }
    }

    private fun parseJsonArray(raw: String): List<String>? {
        return try {
            val start = raw.indexOf('[')
            val end = raw.lastIndexOf(']')
            if (start < 0 || end <= start) return null
            val arr = JSONArray(raw.substring(start, end + 1))
            (0 until arr.length()).map { arr.getString(it).trim() }
        } catch (e: Exception) {
            null
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
        const val QUOTA_EXCEEDED = "API 한도 초과 (무료 등급 분당 15회) — 잠시 후 재시도"
    }
}
