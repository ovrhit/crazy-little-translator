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
import com.example.crazytranslator.repository.TranslationRepository
import com.google.mlkit.genai.common.FeatureStatus
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import com.example.crazytranslator.repository.PersonaPresets

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var translationRepository: TranslationRepository

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
        translationRepository = TranslationRepository(this)
        
        setContent {
            val personaPrompt by preferencesRepository.personaPrompt.collectAsState(initial = "")
            val selectedPreset by preferencesRepository.selectedPreset.collectAsState(initial = PersonaPresets.DEFAULT.name)
            var aiStatus by remember { mutableStateOf("Checking AI Status...") }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val status = translationRepository.checkModelStatus()
                aiStatus = when (status) {
                    FeatureStatus.AVAILABLE -> "AI Model Ready"
                    FeatureStatus.DOWNLOADABLE -> "AI Model Downloadable"
                    FeatureStatus.UNAVAILABLE -> "AI Not Supported on this Device"
                    else -> "AI Status: $status"
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        personaPrompt = personaPrompt,
                        selectedPreset = selectedPreset,
                        aiStatus = aiStatus,
                        onPersonaChange = { newPrompt, presetName ->
                            scope.launch { preferencesRepository.savePersonaPrompt(newPrompt, presetName) }
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
    selectedPreset: String,
    aiStatus: String,
    onPersonaChange: (String, String) -> Unit,
    onStartOverlay: () -> Unit, 
    onCheckPermissions: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Crazy Little Translator", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "AI Status: $aiStatus", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Select Persona Preset", style = MaterialTheme.typography.titleMedium)
        LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            items(PersonaPresets.values()) { preset ->
                FilterChip(
                    selected = selectedPreset == preset.name,
                    onClick = { 
                        if (preset != PersonaPresets.CUSTOM) {
                            onPersonaChange(preset.prompt, preset.name)
                        } else {
                            onPersonaChange(personaPrompt, preset.name)
                        }
                    },
                    label = { Text(preset.label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        
        if (selectedPreset == PersonaPresets.CUSTOM.name) {
            Text(text = "Custom Persona Prompt", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = personaPrompt,
                onValueChange = { onPersonaChange(it, PersonaPresets.CUSTOM.name) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("e.g. Speak like a tsundere anime character") }
            )
        } else {
            val currentPreset = PersonaPresets.values().find { it.name == selectedPreset }
            Text(
                text = "Current: ${currentPreset?.label ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = personaPrompt,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCheckPermissions, modifier = Modifier.fillMaxWidth()) {
            Text("1. Grant Overlay Permission")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onStartOverlay, modifier = Modifier.fillMaxWidth()) {
            Text("2. Start Translation Overlay")
        }
    }
}
