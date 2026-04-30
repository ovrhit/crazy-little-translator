package com.example.crazytranslator.repository

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OcrRepository {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            ""
        }
    }

    // Version that returns structured data for overlay
    suspend fun recognizeTextBlocks(bitmap: Bitmap): List<OcrBlock> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            result.textBlocks.map { block ->
                OcrBlock(
                    text = block.text,
                    boundingBox = block.boundingBox
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class OcrBlock(
    val text: String,
    val boundingBox: android.graphics.Rect?
)
