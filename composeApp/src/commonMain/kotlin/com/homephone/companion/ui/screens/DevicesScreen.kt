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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homephone.companion.data.api.HomephoneApiClient
import com.homephone.companion.data.model.CreateDeviceRequest
import com.homephone.companion.data.model.Device
import kotlinx.coroutines.launch

/**
 * Lists devices via GET /devices and lets the user add/remove devices —
 * this is how one account manages more than one device. All actions call
 * straight through to the REST API; nothing here decides call handling.
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
    var showAddDialog by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<Device?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
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

    LaunchedEffect(Unit) { refresh() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Devices", style = MaterialTheme.typography.titleLarge)
            Row {
                TextButton(onClick = { showAddDialog = true }) { Text("Add") }
                TextButton(onClick = onOpenSettings) { Text("Settings") }
            }
        }

        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            error != null -> Text("Error: $error")
            devices.isEmpty() -> Text("No devices yet. Tap Add to register one.")
            else -> LazyColumn {
                items(devices, key = { it.id }) { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDevice(device.id) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(device.name, style = MaterialTheme.typography.titleMedium)
                            Text("${device.sipEndpointId} · ${device.timezone}")
                        }
                        TextButton(onClick = { deviceToDelete = device }) { Text("Remove") }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { request ->
                scope.launch {
                    try {
                        apiClient.createDevice(request)
                        showAddDialog = false
                        refresh()
                    } catch (t: Throwable) {
                        error = t.message ?: "Failed to create device"
                        showAddDialog = false
                    }
                }
            },
        )
    }

    deviceToDelete?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text("Remove ${device.name}?") },
            text = { Text("This also deletes its contacts, schedules, and call history on the server.") },
            confirmButton = {
                TextButton(onClick = {
                    val target = device
                    deviceToDelete = null
                    scope.launch {
                        try {
                            apiClient.deleteDevice(target.id)
                            refresh()
                        } catch (t: Throwable) {
                            error = t.message ?: "Failed to remove device"
                        }
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateDeviceRequest) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var sipEndpointId by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("UTC") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add device") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(
                    value = sipEndpointId,
                    onValueChange = { sipEndpointId = it },
                    label = { Text("SIP endpoint ID") },
                )
                OutlinedTextField(
                    value = timezone,
                    onValueChange = { timezone = it },
                    label = { Text("Timezone (e.g. America/New_York)") },
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && sipEndpointId.isNotBlank() && timezone.isNotBlank(),
                onClick = {
                    onCreate(
                        CreateDeviceRequest(
                            name = name.trim(),
                            sipEndpointId = sipEndpointId.trim(),
                            timezone = timezone.trim(),
                        ),
                    )
                },
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
