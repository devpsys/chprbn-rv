package ng.com.chprbn.mobile.feature.verification.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [VerifiedSyncApiService] — the legacy
 * per-row upload endpoint. This endpoint is in production today; its
 * batched replacement (§7.4 of the API doc) is To Be Implemented.
 *
 * Pins the snake_case body fields (all five are `@SerializedName`-ed
 * inside `VerifiedSyncRequestDto`), the path / method, and the response
 * envelope parsing.
 */
class VerifiedSyncApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: VerifiedSyncApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, VerifiedSyncApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `syncVerifiedLicense posts to practitioners-verified-sync with snake_case fields`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        api.syncVerifiedLicense(
            VerifiedSyncRequestDto(
                licenseNumber = "MD-99201-B",
                verificationLocation = "Lagos University Teaching Hospital — Ward 4",
                practitionerPresent = true,
                remark = "ID matched; license card presented.",
                verifiedAt = 1_768_515_600_000L,
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/practitioners/verified-sync", recorded.path)
        assertEquals("application/json; charset=UTF-8", recorded.getHeader("Content-Type"))

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("MD-99201-B", body["license_number"].asString)
        assertEquals(
            "Lagos University Teaching Hospital — Ward 4",
            body["verification_location"].asString,
        )
        assertEquals(true, body["practitioner_present"].asBoolean)
        assertEquals("ID matched; license card presented.", body["remark"].asString)
        assertEquals(1_768_515_600_000L, body["verified_at"].asLong)
    }

    @Test
    fun `syncVerifiedLicense parses success envelope with server-assigned id`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "Verification accepted.",
                  "data": {
                    "id": 7421,
                    "license_number": "MD-99201-B",
                    "verified_at": 1768515600000
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.syncVerifiedLicense(
            VerifiedSyncRequestDto("MD-99201-B", "loc", true, "r", 1_768_515_600_000L),
        )

        assertTrue(response.isSuccessful)
        val data = response.body()!!.data!!
        assertEquals(7421L, data.id)
        assertEquals("MD-99201-B", data.license_number)
        assertEquals(1_768_515_600_000L, data.verified_at)
    }

    @Test
    fun `syncVerifiedLicense 422 surfaces errorBody for downstream parsing`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"message":"license_number is required"}"""),
        )

        val response = api.syncVerifiedLicense(
            VerifiedSyncRequestDto("", "loc", true, "r", 0L),
        )

        assertFalse(response.isSuccessful)
        assertEquals(422, response.code())
        assertTrue(
            response.errorBody()?.string().orEmpty().contains("license_number is required"),
        )
    }

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
