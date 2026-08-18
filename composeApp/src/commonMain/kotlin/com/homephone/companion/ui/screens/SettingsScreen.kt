package com.homephone.companion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homephone.companion.data.settings.BackendConfig

/**
 * Milestone 2 has no account system: the user just points this app at
 * their own self-hosted homephone-dev-server instance and pastes in an
 * API token they generated there. Both are stored locally on-device.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    backendConfig: BackendConfig,
    onDone: () -> Unit,
) {
    var baseUrl by remember { mutableStateOf(backendConfig.baseUrl) }
    var apiToken by remember { mutableStateOf(backendConfig.apiToken) }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Backend connection")
        Text("Enter the base URL and API token for your homephone-dev-server instance.")
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL (e.g. https://homephone.example.com)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = apiToken,
            onValueChange = { apiToken = it },
            label = { Text("API token") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                backendConfig.baseUrl = baseUrl.trim()
                backendConfig.apiToken = apiToken.trim()
                onDone()
            },
            enabled = baseUrl.isNotBlank() && apiToken.isNotBlank(),
        ) {
            Text("Save")
        }
    }
}
