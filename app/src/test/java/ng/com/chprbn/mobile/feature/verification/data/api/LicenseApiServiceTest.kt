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
 * JSON-wire integration tests for [LicenseApiService]. Pins the
 * `license_number` query param + path / method, and the snake_case
 * response envelope including the nested `institution_attended` object.
 *
 * This is the most heavily-relied-on read endpoint in production —
 * silent drift here would corrupt every license lookup the officer
 * performs, so wire-level coverage matters.
 */
class LicenseApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: LicenseApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, LicenseApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getLicenseRecord GETs practitioners-license with license_number query param`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        api.getLicenseRecord(licenseNumber = "MD-99201-B")

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/practitioners/license?license_number=MD-99201-B", recorded.path)
    }

    @Test
    fun `getLicenseRecord url-encodes license numbers containing reserved chars`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        api.getLicenseRecord(licenseNumber = "MD 99201/B")

        val recorded = server.takeRequest()
        assertEquals(
            "/practitioners/license?license_number=MD%2099201%2FB",
            recorded.path,
        )
    }

    @Test
    fun `getLicenseRecord parses every documented snake_case field`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "OK",
                  "data": {
                    "registration_number": "MD-99201-B",
                    "full_name": "Dr. Sarah Jenkins",
                    "photo": "iVBORw0KGgo",
                    "photo_url": "https://app.chprbn.gov.ng/media/md99201b.jpg",
                    "profession": "Medical Doctor",
                    "certificate_no": "CERT-001",
                    "email": "sarah@example.com",
                    "phone": "+2348012345678",
                    "license_status": "Active",
                    "expiry_date": "2026-10-31",
                    "subtitle": "General Practice",
                    "issue_date": "2024-01-15",
                    "gender": "Female",
                    "graduation_date": "2010-06-30",
                    "institution_attended": { "name": "University of Ibadan, College of Medicine" }
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.getLicenseRecord("MD-99201-B")

        val data = response.body()!!.data!!
        assertEquals("MD-99201-B", data.registration_number)
        assertEquals("Dr. Sarah Jenkins", data.full_name)
        assertEquals("iVBORw0KGgo", data.photo)
        assertEquals("https://app.chprbn.gov.ng/media/md99201b.jpg", data.photoUrl)
        assertEquals("Medical Doctor", data.profession)
        assertEquals("CERT-001", data.certificate_no)
        assertEquals("sarah@example.com", data.email)
        assertEquals("+2348012345678", data.phone)
        assertEquals("Active", data.license_status)
        assertEquals("2026-10-31", data.expiry_date)
        assertEquals("General Practice", data.subtitle)
        assertEquals("2024-01-15", data.issue_date)
        assertEquals("Female", data.gender)
        assertEquals("2010-06-30", data.graduation_date)
        assertEquals("University of Ibadan, College of Medicine", data.institution_attended?.name)
    }

    @Test
    fun `getLicenseRecord tolerates a null data field for 'no record'`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"no record","data":null}"""))

        val response = api.getLicenseRecord("UNKNOWN")

        assertTrue(response.isSuccessful)
        assertNull(response.body()!!.data)
    }

    @Test
    fun `getLicenseRecord 404 surfaces as non-successful`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody(""))

        val response = api.getLicenseRecord("MISSING")

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
