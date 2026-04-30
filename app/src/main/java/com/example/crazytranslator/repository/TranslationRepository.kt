package com.example.crazytranslator.repository

import com.example.crazytranslator.Secret
import com.google.ai.client.generativeai.GenerativeModel

class TranslationRepository {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Secret.GEMINI_API_KEY
    )

    suspend fun translateText(text: String, personaPrompt: String, screenContext: String = ""): String {
        if (text.isBlank()) return ""
        
        val prompt = """
            Context & Persona Goal:
            1. User's Base Preference: $personaPrompt
            2. Task: Translate the 'Target Text' into Korean.
            3. Global Screen Context: 
               ---
               $screenContext
               ---
            4. Dynamic Persona Adaptation: 
               - Use the 'Global Screen Context' to identify who is speaking (look for names, titles, relationship markers).
               - Analyze the linguistic cues in the 'Target Text' (sentence endings like '~desu', '~noda', '~ze', honorifics, etc.).
               - If the speaker is a known character or has a distinct personality, adapt the Korean translation to reflect their unique 'persona' (tone, politeness level, vocabulary).
               - Combine these insights with the 'User's Base Preference'.
            
            Target Text:
            ---
            $text
            ---
            
            Return only the translated Korean text.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
