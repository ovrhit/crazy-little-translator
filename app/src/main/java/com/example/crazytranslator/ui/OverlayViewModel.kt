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

    fun updateOcrResults(blocks: List<OcrBlock>, personaPrompt: String, screenContext: String) {
        viewModelScope.launch {
            try {
                _errorState.value = null
                val translated = blocks.map { block ->
                    val translatedText = translationRepository.translateText(
                        text = block.text,
                        personaPrompt = personaPrompt,
                        screenContext = screenContext
                    )
                    if (translatedText.startsWith("Error:")) {
                        _errorState.value = translatedText
                    }
                    TranslatedBlock(
                        originalText = block.text,
                        translatedText = translatedText,
                        boundingBox = block.boundingBox
                    )
                }
                _translatedBlocks.value = translated
            } catch (e: Exception) {
                _errorState.value = "Connection Error"
            }
        }
    }
}
