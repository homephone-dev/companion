package com.homephone.companion.data.api

import com.homephone.companion.data.model.CreateContactRequest
import com.homephone.companion.data.model.CreateScheduleRequest
import com.homephone.companion.data.model.ScheduleMode
import com.homephone.companion.data.model.UpdateScheduleRequest
import com.homephone.companion.data.settings.BackendConfig
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Request/response mapping tests for [HomephoneApiClient] against a Ktor
 * MockEngine, checked against the shapes documented in
 * homephone-dev-server/docs/api.md. These only verify HTTP method, path,
 * headers, and JSON (de)serialization — never any allow/block/schedule
 * decision, since this client makes none.
 */
class HomephoneApiClientTest {

    private fun buildClient(
        response: (HttpRequestData) -> Triple<String, HttpStatusCode, String>,
    ): HomephoneApiClient {
        val engine = MockEngine { request ->
            val (body, status, contentType) = response(request)
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, contentType),
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
        val config = BackendConfig(MapSettings()).apply {
            baseUrl = "https://homephone.example.com"
            apiToken = "test-token"
        }
        return HomephoneApiClient(config, httpClient)
    }

    @Test
    fun listDevices_parsesJsonArray_andHitsExpectedPath() = runTest {
        var seenPath: String? = null
        var seenAuth: String? = null
        val client = buildClient { request ->
            seenPath = request.url.encodedPath
            seenAuth = request.headers[HttpHeaders.Authorization]
            Triple(
                """[{"id":"d1","name":"Kitchen","sipEndpointId":"sip1","timezone":"UTC","createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}]""",
                HttpStatusCode.OK,
                "application/json",
            )
        }

        val devices = client.listDevices()

        assertEquals("/api/v1/devices", seenPath)
        assertEquals("Bearer test-token", seenAuth)
        assertEquals(1, devices.size)
        assertEquals("Kitchen", devices[0].name)
    }

    @Test
    fun createContact_postsToDeviceContactsPath_withLabelAndNumber() = runTest {
        var seenPath: String? = null
        var seenMethod: String? = null
        var seenBody: String? = null
        val client = buildClient { request ->
            seenPath = request.url.encodedPath
            seenMethod = request.method.value
            seenBody = (request.body as io.ktor.http.content.TextContent).text
            Triple(
                """{"id":"c1","deviceId":"d1","label":"Mom","number":"+15551234567","createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}""",
                HttpStatusCode.Created,
                "application/json",
            )
        }

        val contact = client.createContact("d1", CreateContactRequest(label = "Mom", number = "+15551234567"))

        assertEquals("/api/v1/devices/d1/contacts", seenPath)
        assertEquals("POST", seenMethod)
        assertTrue(seenBody!!.contains("\"label\":\"Mom\""))
        assertTrue(seenBody!!.contains("\"number\":\"+15551234567\""))
        assertEquals("Mom", contact.label)
    }

    @Test
    fun updateSchedule_sendsHasDayOfWeekFlag_whenClearingDay() = runTest {
        var seenBody: String? = null
        val client = buildClient { request ->
            seenBody = (request.body as io.ktor.http.content.TextContent).text
            Triple(
                """{"id":"s1","deviceId":"d1","dayOfWeek":null,"startTime":"22:00:00","endTime":"07:00:00","mode":"block"}""",
                HttpStatusCode.OK,
                "application/json",
            )
        }

        val schedule = client.updateSchedule(
            "s1",
            UpdateScheduleRequest(dayOfWeek = null, hasDayOfWeek = true, startTime = null, endTime = null, mode = null),
        )

        assertTrue(seenBody!!.contains("\"hasDayOfWeek\":true"))
        assertEquals(null, schedule.dayOfWeek)
        assertEquals(ScheduleMode.BLOCK, schedule.mode)
    }

    @Test
    fun createSchedule_postsToDeviceSchedulesPath() = runTest {
        var seenPath: String? = null
        val client = buildClient { request ->
            seenPath = request.url.encodedPath
            Triple(
                """{"id":"s2","deviceId":"d1","dayOfWeek":1,"startTime":"08:00:00","endTime":"09:00:00","mode":"allow"}""",
                HttpStatusCode.Created,
                "application/json",
            )
        }

        client.createSchedule("d1", CreateScheduleRequest(dayOfWeek = 1, startTime = "08:00:00", endTime = "09:00:00", mode = ScheduleMode.ALLOW))

        assertEquals("/api/v1/devices/d1/schedules", seenPath)
    }

    @Test
    fun listCalls_setsSinceAndLimitQueryParams() = runTest {
        var seenQuery: String? = null
        val client = buildClient { request ->
            seenQuery = request.url.encodedQuery
            Triple("[]", HttpStatusCode.OK, "application/json")
        }

        client.listCalls("d1", since = "2026-01-01T00:00:00Z", limit = 10)

        assertTrue(seenQuery!!.contains("since="))
        assertTrue(seenQuery!!.contains("limit=10"))
    }

    @Test
    fun deleteDevice_usesDeleteMethod_onDevicePath() = runTest {
        var seenPath: String? = null
        var seenMethod: String? = null
        val client = buildClient { request ->
            seenPath = request.url.encodedPath
            seenMethod = request.method.value
            Triple("", HttpStatusCode.NoContent, "application/json")
        }

        client.deleteDevice("d1")

        assertEquals("/api/v1/devices/d1", seenPath)
        assertEquals("DELETE", seenMethod)
    }
}
