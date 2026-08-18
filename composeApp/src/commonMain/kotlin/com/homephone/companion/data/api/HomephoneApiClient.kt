package com.homephone.companion.data.api

import com.homephone.companion.data.model.CallLog
import com.homephone.companion.data.model.Contact
import com.homephone.companion.data.model.CreateContactRequest
import com.homephone.companion.data.model.CreateDeviceRequest
import com.homephone.companion.data.model.CreateScheduleRequest
import com.homephone.companion.data.model.Device
import com.homephone.companion.data.model.Schedule
import com.homephone.companion.data.model.UpdateContactRequest
import com.homephone.companion.data.model.UpdateDeviceRequest
import com.homephone.companion.data.model.UpdateScheduleRequest
import com.homephone.companion.data.settings.BackendConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Thin HTTP client over the homephone-dev-server REST API
 * (see homephone-dev-server/docs/api.md). This client performs no
 * allow/block/scheduling decisions — it only serializes requests and
 * deserializes responses for whatever the server already decided.
 *
 * Base URL and bearer token come from [BackendConfig], set by the user
 * in the Settings screen — there is no separate account/auth layer.
 */
class HomephoneApiClient(
    private val config: BackendConfig,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    private fun apiBase(): String = config.baseUrl.trimEnd('/') + "/api/v1"

    // ---- Devices ----

    suspend fun listDevices(): List<Device> =
        httpClient.get(apiBase() + "/devices") { authorize() }.body()

    suspend fun createDevice(request: CreateDeviceRequest): Device =
        httpClient.post(apiBase() + "/devices") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getDevice(id: String): Device =
        httpClient.get(apiBase() + "/devices/$id") { authorize() }.body()

    suspend fun updateDevice(id: String, request: UpdateDeviceRequest): Device =
        httpClient.patch(apiBase() + "/devices/$id") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteDevice(id: String) {
        httpClient.delete(apiBase() + "/devices/$id") { authorize() }
    }

    // ---- Contacts ----

    suspend fun listContacts(deviceId: String): List<Contact> =
        httpClient.get(apiBase() + "/devices/$deviceId/contacts") { authorize() }.body()

    suspend fun createContact(deviceId: String, request: CreateContactRequest): Contact =
        httpClient.post(apiBase() + "/devices/$deviceId/contacts") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateContact(id: String, request: UpdateContactRequest): Contact =
        httpClient.patch(apiBase() + "/contacts/$id") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteContact(id: String) {
        httpClient.delete(apiBase() + "/contacts/$id") { authorize() }
    }

    // ---- Schedules ----

    suspend fun listSchedules(deviceId: String): List<Schedule> =
        httpClient.get(apiBase() + "/devices/$deviceId/schedules") { authorize() }.body()

    suspend fun createSchedule(deviceId: String, request: CreateScheduleRequest): Schedule =
        httpClient.post(apiBase() + "/devices/$deviceId/schedules") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateSchedule(id: String, request: UpdateScheduleRequest): Schedule =
        httpClient.patch(apiBase() + "/schedules/$id") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteSchedule(id: String) {
        httpClient.delete(apiBase() + "/schedules/$id") { authorize() }
    }

    // ---- Calls ----

    suspend fun listCalls(deviceId: String, since: String? = null, limit: Int? = null): List<CallLog> =
        httpClient.get(apiBase() + "/devices/$deviceId/calls") {
            authorize()
            since?.let { parameter("since", it) }
            limit?.let { parameter("limit", it) }
        }.body()

    suspend fun getCall(id: String): CallLog =
        httpClient.get(apiBase() + "/calls/$id") { authorize() }.body()

    suspend fun deleteCall(id: String) {
        httpClient.delete(apiBase() + "/calls/$id") { authorize() }
    }

    /** Erasure/purge by number and/or age cutoff — at least one of [number]/[before] is required. */
    suspend fun purgeCalls(deviceId: String, number: String? = null, before: String? = null) {
        require(number != null || before != null) { "at least one of number/before is required" }
        httpClient.delete(apiBase() + "/devices/$deviceId/calls") {
            authorize()
            number?.let { parameter("number", it) }
            before?.let { parameter("before", it) }
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.authorize() {
        header("Authorization", "Bearer ${config.apiToken}")
    }
}

fun defaultHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
