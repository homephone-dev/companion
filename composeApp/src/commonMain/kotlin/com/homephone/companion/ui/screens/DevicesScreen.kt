package com.homephone.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homephone.companion.data.api.HomephoneApiClient
import com.homephone.companion.data.model.Device
import kotlinx.coroutines.launch

/**
 * Lists devices via GET /devices. This is the one screen actually wired
 * end-to-end to the API client — proof the shared-UI + Ktor plumbing works.
 */
@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    apiClient: HomephoneApiClient,
    onOpenDevice: (deviceId: String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScopeCompat()

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            error = null
            try {
                devices = apiClient.listDevices()
            } catch (t: Throwable) {
                error = t.message ?: "Failed to load devices"
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Devices", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            TextButton(onClick = onOpenSettings) { Text("Settings") }
        }

        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            error != null -> Text("Error: $error")
            devices.isEmpty() -> Text("No devices yet.")
            else -> LazyColumn {
                items(devices) { device ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDevice(device.id) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(device.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        Text("${device.sipEndpointId} · ${device.timezone}")
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
