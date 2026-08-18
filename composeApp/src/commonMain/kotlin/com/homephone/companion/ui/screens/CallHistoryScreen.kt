package com.homephone.companion.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homephone.companion.data.api.HomephoneApiClient
import com.homephone.companion.data.model.CallLog
import kotlinx.coroutines.launch

/**
 * Read-only view of GET /devices/{id}/calls (newest first). No purge/erasure
 * UI wired up yet — the API client already exposes deleteCall/purgeCalls for
 * a later pass.
 */
@Composable
fun CallHistoryScreen(
    modifier: Modifier = Modifier,
    deviceId: String,
    apiClient: HomephoneApiClient,
    onBack: () -> Unit,
) {
    var calls by remember { mutableStateOf<List<CallLog>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceId) {
        scope.launch {
            loading = true
            error = null
            try {
                calls = apiClient.listCalls(deviceId, limit = 50)
            } catch (t: Throwable) {
                error = t.message ?: "Failed to load call history"
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Back") }
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            error != null -> Text("Error: $error")
            calls.isEmpty() -> Text("No calls yet.")
            else -> LazyColumn {
                items(calls) { call ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("${call.direction} · ${call.remoteNumber} · ${call.outcome}")
                        Text("${call.startedAt}" + (call.durationSeconds?.let { " · ${it}s" } ?: ""))
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
