package com.homephone.companion.ui.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.homephone.companion.data.model.Contact
import com.homephone.companion.data.model.CreateContactRequest
import com.homephone.companion.data.model.CreateScheduleRequest
import com.homephone.companion.data.model.Schedule
import com.homephone.companion.data.model.ScheduleMode
import com.homephone.companion.data.model.UpdateContactRequest
import com.homephone.companion.data.model.UpdateScheduleRequest
import kotlinx.coroutines.launch

private val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

/**
 * Device detail: approved contacts (allowlist entries) and the quiet-hours
 * schedule for this device. All CRUD here maps directly to the server's
 * /devices/{id}/contacts and /devices/{id}/schedules endpoints — this
 * screen makes no allow/block decisions itself, it only edits what the
 * server stores. Which numbers are allowed and when quiet hours apply is
 * entirely server-side; this UI only sends the edits the user makes.
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

    var showAddContact by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    var showAddSchedule by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<Schedule?>(null) }
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun refresh() {
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

    LaunchedEffect(deviceId) { refresh() }

    fun runAndRefresh(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
                refresh()
            } catch (t: Throwable) {
                error = t.message ?: "Request failed"
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Contacts", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddContact = true }) { Text("Add") }
                }
                if (contacts.isEmpty()) {
                    Text("No contacts yet.")
                } else {
                    LazyColumn {
                        items(contacts, key = { it.id }) { contact ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${contact.label} — ${contact.number}")
                                Row {
                                    TextButton(onClick = { contactToEdit = contact }) { Text("Edit") }
                                    TextButton(onClick = { contactToDelete = contact }) { Text("Delete") }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Schedule (quiet hours)", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddSchedule = true }) { Text("Add") }
                }
                if (schedules.isEmpty()) {
                    Text("No schedule entries yet.")
                } else {
                    LazyColumn {
                        items(schedules, key = { it.id }) { schedule ->
                            val day = schedule.dayOfWeek?.let { dayNames.getOrElse(it) { it.toString() } } ?: "Every day"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("$day ${schedule.startTime}-${schedule.endTime}: ${schedule.mode}")
                                Row {
                                    TextButton(onClick = { scheduleToEdit = schedule }) { Text("Edit") }
                                    TextButton(onClick = { scheduleToDelete = schedule }) { Text("Delete") }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showAddContact) {
        ContactDialog(
            title = "Add contact",
            initialLabel = "",
            initialNumber = "",
            onDismiss = { showAddContact = false },
            onSave = { label, number ->
                showAddContact = false
                runAndRefresh { apiClient.createContact(deviceId, CreateContactRequest(label, number)) }
            },
        )
    }

    contactToEdit?.let { contact ->
        ContactDialog(
            title = "Edit contact",
            initialLabel = contact.label,
            initialNumber = contact.number,
            onDismiss = { contactToEdit = null },
            onSave = { label, number ->
                contactToEdit = null
                runAndRefresh {
                    apiClient.updateContact(contact.id, UpdateContactRequest(label = label, number = number))
                }
            },
        )
    }

    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = { Text("Delete ${contact.label}?") },
            confirmButton = {
                TextButton(onClick = {
                    contactToDelete = null
                    runAndRefresh { apiClient.deleteContact(contact.id) }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { contactToDelete = null }) { Text("Cancel") } },
        )
    }

    if (showAddSchedule) {
        ScheduleDialog(
            title = "Add schedule",
            initial = null,
            onDismiss = { showAddSchedule = false },
            onSave = { dayOfWeek, startTime, endTime, mode ->
                showAddSchedule = false
                runAndRefresh {
                    apiClient.createSchedule(deviceId, CreateScheduleRequest(dayOfWeek, startTime, endTime, mode))
                }
            },
        )
    }

    scheduleToEdit?.let { schedule ->
        ScheduleDialog(
            title = "Edit schedule",
            initial = schedule,
            onDismiss = { scheduleToEdit = null },
            onSave = { dayOfWeek, startTime, endTime, mode ->
                scheduleToEdit = null
                runAndRefresh {
                    apiClient.updateSchedule(
                        schedule.id,
                        UpdateScheduleRequest(
                            dayOfWeek = dayOfWeek,
                            hasDayOfWeek = true,
                            startTime = startTime,
                            endTime = endTime,
                            mode = mode,
                        ),
                    )
                }
            },
        )
    }

    scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Delete this schedule entry?") },
            confirmButton = {
                TextButton(onClick = {
                    scheduleToDelete = null
                    runAndRefresh { apiClient.deleteSchedule(schedule.id) }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { scheduleToDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ContactDialog(
    title: String,
    initialLabel: String,
    initialNumber: String,
    onDismiss: () -> Unit,
    onSave: (label: String, number: String) -> Unit,
) {
    var label by remember { mutableStateOf(initialLabel) }
    var number by remember { mutableStateOf(initialNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") })
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Number (E.164, e.g. +15551234567)") },
                )
            }
        },
        confirmButton = {
            Button(
                enabled = label.isNotBlank() && number.isNotBlank(),
                onClick = { onSave(label.trim(), number.trim()) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ScheduleDialog(
    title: String,
    initial: Schedule?,
    onDismiss: () -> Unit,
    onSave: (dayOfWeek: Int?, startTime: String, endTime: String, mode: ScheduleMode) -> Unit,
) {
    var dayOfWeek by remember { mutableStateOf(initial?.dayOfWeek) }
    var startTime by remember { mutableStateOf(initial?.startTime ?: "22:00:00") }
    var endTime by remember { mutableStateOf(initial?.endTime ?: "07:00:00") }
    var mode by remember { mutableStateOf(initial?.mode ?: ScheduleMode.BLOCK) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var modeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row {
                    TextButton(onClick = { dayMenuExpanded = true }) {
                        Text("Day: " + (dayOfWeek?.let { dayNames.getOrElse(it) { it.toString() } } ?: "Every day"))
                    }
                    DropdownMenu(expanded = dayMenuExpanded, onDismissRequest = { dayMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Every day") }, onClick = { dayOfWeek = null; dayMenuExpanded = false })
                        dayNames.forEachIndexed { index, name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { dayOfWeek = index; dayMenuExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start time (HH:MM:SS)") },
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End time (HH:MM:SS)") },
                )
                Row {
                    TextButton(onClick = { modeMenuExpanded = true }) {
                        Text("Mode: ${mode.name.lowercase()}")
                    }
                    DropdownMenu(expanded = modeMenuExpanded, onDismissRequest = { modeMenuExpanded = false }) {
                        ScheduleMode.entries.forEach { m ->
                            DropdownMenuItem(text = { Text(m.name.lowercase()) }, onClick = { mode = m; modeMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = startTime.isNotBlank() && endTime.isNotBlank(),
                onClick = { onSave(dayOfWeek, startTime.trim(), endTime.trim(), mode) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
