package ng.com.chprbn.mobile.feature.verification.data.api

import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [VerificationApiService]. Pins the
 * `/dashboard/profile` path + GET method + tutor `ProfileEnvelopeDto`
 * shape (separate from adhoc — the practitioner-tutor profile has
 * `permissions`, `unit`, and `lastLoginAt` that the adhoc envelope
 * doesn't carry).
 */
class VerificationApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: VerificationApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, VerificationApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getProfile GETs dashboard-profile`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":{"id":"u1","name":"X","username":"x","email":"x@x"}}"""))

        api.getProfile()

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/dashboard/profile", recorded.path)
    }

    @Test
    fun `getProfile parses every documented tutor field`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "OK",
                  "data": {
                    "id": "usr_8f3c21a9",
                    "photo": "iVBORw0KGgo",
                    "name": "Dr. Sarah Jenkins",
                    "gender": "Female",
                    "username": "MD-99201-B",
                    "email": "sarah@chprbn.gov.ng",
                    "phone": "+2348012345678",
                    "permissions": ["verify_practitioner", "sync_records"],
                    "role": "Practitioner Tutor",
                    "unit": "Lagos University Teaching Hospital",
                    "lastLoginAt": "2026-05-15T08:30:00Z"
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.getProfile()

        val data = response.body()!!.data!!
        assertEquals("usr_8f3c21a9", data.id)
        assertEquals("iVBORw0KGgo", data.photo)
        assertEquals("Dr. Sarah Jenkins", data.name)
        assertEquals("Female", data.gender)
        assertEquals("MD-99201-B", data.username)
        assertEquals("sarah@chprbn.gov.ng", data.email)
        assertEquals("+2348012345678", data.phone)
        assertEquals(listOf("verify_practitioner", "sync_records"), data.permissions)
        assertEquals("Practitioner Tutor", data.role)
        assertEquals("Lagos University Teaching Hospital", data.unit)
        assertEquals("2026-05-15T08:30:00Z", data.lastLoginAt)
    }

    @Test
    fun `getProfile leaves permissions null when JSON omits the field (Gson gotcha)`() = runTest {
        // Regression-pin: ProfileDataDto declares `permissions: List<String> = emptyList()`
        // but Gson's reflective deserialization does NOT honor Kotlin
        // constructor defaults. When the server omits the field, `permissions`
        // ends up as a null reference despite the non-null declared type.
        // Any caller that dereferences it (e.g. `.size`, `.isEmpty()`) will NPE.
        // Latent bug — flag for a follow-up that either makes the field nullable
        // (`List<String>? = null`) and `.orEmpty()`s at the consumer, or wires
        // a Kotlin-aware Gson adapter. Until then, backend MUST always return
        // a `permissions` array (possibly empty).
        server.enqueue(
            jsonOk(
                """
                {"status":true,"data":{"id":"u1","name":"X","username":"x","email":"x@x"}}
                """.trimIndent(),
            ),
        )

        val response = api.getProfile()

        @Suppress("UNCHECKED_CAST")
        val permissions = response.body()!!.data!!.permissions as List<String>?
        assertNull("Gson leaves omitted list-typed fields null", permissions)
    }

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
