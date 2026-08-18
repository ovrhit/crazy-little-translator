package com.example.crazytranslator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crazytranslator.repository.OcrBlock
import com.example.crazytranslator.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranslatedBlock(
    val originalText: String,
    val translatedText: String,
    val boundingBox: android.graphics.Rect?
)

class OverlayViewModel(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _translatedBlocks = MutableStateFlow<List<TranslatedBlock>>(emptyList())
    val translatedBlocks: StateFlow<List<TranslatedBlock>> = _translatedBlocks.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // Simple cache: Map<OriginalText, TranslatedText>
    private val translationCache = mutableMapOf<String, String>()
    private var lastPersonaPrompt = ""

    fun updateOcrResults(blocks: List<OcrBlock>, personaPrompt: String, screenContext: String) {
        // Clear cache if persona changed significantly
        if (personaPrompt != lastPersonaPrompt) {
            translationCache.clear()
            lastPersonaPrompt = personaPrompt
        }

        viewModelScope.launch {
            try {
                _errorState.value = null

                // Translate only blocks we haven't cached yet — in ONE batched request.
                val uncached = blocks.map { it.text }.distinct().filter { it.isNotBlank() && it !in translationCache }
                if (uncached.isNotEmpty()) {
                    val results = translationRepository.translateBatch(uncached, personaPrompt, screenContext)
                    uncached.forEachIndexed { i, source ->
                        val out = results.getOrNull(i).orEmpty()
                        if (isFailure(out)) {
                            _errorState.value = out
                        } else if (out.isNotBlank()) {
                            translationCache[source] = out
                        }
                    }
                }

                _translatedBlocks.value = blocks.map { block ->
                    TranslatedBlock(
                        originalText = block.text,
                        translatedText = translationCache[block.text] ?: "",
                        boundingBox = block.boundingBox
                    )
                }
            } catch (e: Exception) {
                _errorState.value = "Connection Error"
            }
        }
    }

    private fun isFailure(text: String): Boolean =
        text.startsWith("Error:") || text.startsWith("API ")
}
