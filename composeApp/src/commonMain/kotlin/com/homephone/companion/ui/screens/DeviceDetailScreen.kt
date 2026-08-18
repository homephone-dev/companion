package com.homephone.companion.ui.screens

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
import androidx.compose.material3.MaterialTheme
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
import com.homephone.companion.data.model.Contact
import com.homephone.companion.data.model.Schedule
import kotlinx.coroutines.launch

/**
 * Device detail: approved contacts (allowlist entries) and the quiet-hours
 * schedule for this device. All CRUD here maps directly to the server's
 * /devices/{id}/contacts and /devices/{id}/schedules endpoints — this
 * screen makes no allow/block decisions itself, it only edits what the
 * server stores.
 */
@Composable
fun DeviceDetailScreen(
    modifier: Modifier = Modifier,
    deviceId: String,
    apiClient: HomephoneApiClient,
    onBack: () -> Unit,
    onOpenCallHistory: () -> Unit,
) {
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceId) {
        scope.launch {
            loading = true
            error = null
            try {
                contacts = apiClient.listContacts(deviceId)
                schedules = apiClient.listSchedules(deviceId)
            } catch (t: Throwable) {
                error = t.message ?: "Failed to load device"
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("< Devices") }
            TextButton(onClick = onOpenCallHistory) { Text("Call history") }
        }

        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            error != null -> Text("Error: $error")
            else -> {
                Text("Contacts", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(contacts) { contact ->
                        Text("${contact.label} — ${contact.number}", modifier = Modifier.padding(vertical = 6.dp))
                        HorizontalDivider()
                    }
                }
                Text("Schedule", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                LazyColumn {
                    items(schedules) { schedule ->
                        val day = schedule.dayOfWeek?.toString() ?: "every day"
                        Text(
                            "$day ${schedule.startTime}-${schedule.endTime}: ${schedule.mode}",
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
