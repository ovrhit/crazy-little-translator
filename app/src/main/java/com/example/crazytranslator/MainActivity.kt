package com.example.crazytranslator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crazytranslator.service.OverlayService

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.crazytranslator.repository.PreferencesRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepository: PreferencesRepository

    private val projectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(OverlayService.EXTRA_DATA, result.data)
            }
            startForegroundService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepository = PreferencesRepository(this)
        
        setContent {
            val personaPrompt by preferencesRepository.personaPrompt.collectAsState(initial = "")
            val scope = rememberCoroutineScope()

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        personaPrompt = personaPrompt,
                        onPersonaChange = { newPrompt ->
                            scope.launch { preferencesRepository.savePersonaPrompt(newPrompt) }
                        },
                        onStartOverlay = { startScreenCapture() },
                        onCheckPermissions = { checkPermissions() }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startScreenCapture() {
        if (Settings.canDrawOverlays(this)) {
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        } else {
            checkPermissions()
        }
    }

    companion object {
        private const val OVERLAY_PERMISSION_REQ_CODE = 1234
    }
}

@Composable
fun MainScreen(
    personaPrompt: String,
    onPersonaChange: (String) -> Unit,
    onStartOverlay: () -> Unit, 
    onCheckPermissions: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Crazy Little Translator", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Character Persona Prompt", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = personaPrompt,
            onValueChange = onPersonaChange,
            modifier = Modifier.fillMaxSize(fraction = 0.5f),
            label = { Text("e.g. Speak like a tsundere anime character") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCheckPermissions) {
            Text("1. Grant Overlay Permission")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onStartOverlay) {
            Text("2. Start Translation Overlay")
        }
    }
}
