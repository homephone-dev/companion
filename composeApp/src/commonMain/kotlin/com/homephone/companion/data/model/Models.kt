package com.homephone.companion.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * These types mirror the JSON shapes documented in
 * homephone-dev-server/docs/api.md exactly. Do not add fields or
 * endpoints that aren't documented there — this app has no business
 * logic of its own, it only reflects what the server returns.
 */

@Serializable
data class Device(
    val id: String,
    val name: String,
    val sipEndpointId: String,
    val timezone: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateDeviceRequest(
    val name: String,
    val sipEndpointId: String,
    val timezone: String,
)

@Serializable
data class UpdateDeviceRequest(
    val name: String? = null,
    val timezone: String? = null,
)

@Serializable
data class Contact(
    val id: String,
    val deviceId: String,
    val label: String,
    val number: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateContactRequest(
    val label: String,
    val number: String,
)

@Serializable
data class UpdateContactRequest(
    val label: String? = null,
    val number: String? = null,
)

enum class ScheduleMode {
    @SerialName("allow") ALLOW,
    @SerialName("block") BLOCK,
}

@Serializable
data class Schedule(
    val id: String,
    val deviceId: String,
    val dayOfWeek: Int? = null,
    val startTime: String,
    val endTime: String,
    val mode: ScheduleMode,
)

@Serializable
data class CreateScheduleRequest(
    val dayOfWeek: Int? = null,
    val startTime: String,
    val endTime: String,
    val mode: ScheduleMode,
)

@Serializable
data class UpdateScheduleRequest(
    val dayOfWeek: Int? = null,
    val hasDayOfWeek: Boolean? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val mode: ScheduleMode? = null,
)

enum class CallDirection {
    @SerialName("inbound") INBOUND,
    @SerialName("outbound") OUTBOUND,
}

enum class CallOutcome {
    @SerialName("connected") CONNECTED,
    @SerialName("blocked") BLOCKED,
    @SerialName("missed") MISSED,
    @SerialName("failed") FAILED,
}

@Serializable
data class CallLog(
    val id: String,
    val deviceId: String,
    val direction: CallDirection,
    val remoteNumber: String,
    val startedAt: String,
    val answeredAt: String? = null,
    val endedAt: String? = null,
    val durationSeconds: Long? = null,
    val outcome: CallOutcome,
    val reason: String? = null,
)
