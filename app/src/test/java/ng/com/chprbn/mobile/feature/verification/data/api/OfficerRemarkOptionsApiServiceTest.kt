package ng.com.chprbn.mobile.feature.verification.data.api

import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [OfficerRemarkOptionsApiService].
 * Pins the path, method, and the new-convention envelope
 * (`success` + `data.options`).
 */
class OfficerRemarkOptionsApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OfficerRemarkOptionsApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, OfficerRemarkOptionsApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getOfficerRemarkOptions GETs practitioners-officer-remark-options`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"options":[]}}"""))

        api.getOfficerRemarkOptions()

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/practitioners/officer-remark-options", recorded.path)
    }

    @Test
    fun `getOfficerRemarkOptions parses the success envelope with options`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "success": true,
                  "message": "OK",
                  "data": {
                    "options": [
                      "Active",
                      "Expired",
                      "Long Overdue",
                      "Fake"
                    ]
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.getOfficerRemarkOptions()

        assertTrue(response.isSuccessful)
        val options = response.body()!!.data!!.options
        assertEquals(4, options!!.size)
        assertEquals("Active", options.first())
        assertEquals("Fake", options.last())
    }

    @Test
    fun `getOfficerRemarkOptions tolerates an empty options array`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"options":[]}}"""))

        val response = api.getOfficerRemarkOptions()

        assertTrue(response.body()!!.data!!.options!!.isEmpty())
    }

    @Test
    fun `getOfficerRemarkOptions leaves options null when JSON omits the field`() = runTest {
        // Same Gson + Kotlin-defaults gotcha as the tutor profile envelope.
        // The DTO field is intentionally nullable so this lands cleanly.
        server.enqueue(jsonOk("""{"success":true,"data":{}}"""))

        val response = api.getOfficerRemarkOptions()

        assertNull(response.body()!!.data!!.options)
    }

    @Test
    fun `getOfficerRemarkOptions 500 surfaces as non-successful`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val response = api.getOfficerRemarkOptions()

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
