package com.example.crazytranslator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverlayContent(viewModel: OverlayViewModel) {
    val translatedBlocks by viewModel.translatedBlocks.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        translatedBlocks.forEach { block ->
            block.boundingBox?.let { rect ->
                val left = with(density) { rect.left.toDp() }
                val top = with(density) { rect.top.toDp() }
                val width = with(density) { (rect.right - rect.left).toDp() }
                val height = with(density) { (rect.bottom - rect.top).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = block.translatedText,
                        color = Color.Black,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
        
        errorState?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
                    .background(Color.Red.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(text = error, color = Color.White)
            }
        }
    }
}
