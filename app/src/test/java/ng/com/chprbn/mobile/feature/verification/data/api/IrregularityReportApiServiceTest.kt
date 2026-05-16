package ng.com.chprbn.mobile.feature.verification.data.api

import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [IrregularityReportApiService] — the
 * multipart submission endpoint that carries officer-flagged evidence
 * (photo + free-text fields). The **form-data part names**
 * (`name_on_card`, `license_number`, `cadre`, `gender`, `remark`,
 * `reported_at`, `snapshot`) are silently breakable today — if anyone
 * renames a `@Part` annotation on the interface, the server reads
 * different keys and the report writes empty fields with no visible
 * client error. These tests pin every part name + the snapshot's
 * filename + Content-Type at the wire level so a regression fails CI
 * loudly.
 *
 * Asserts on raw multipart body text rather than wiring up
 * okhttp's `MultipartReader` — the part-header substrings are stable
 * across okhttp versions and the test reads clearer.
 */
class IrregularityReportApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: IrregularityReportApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, IrregularityReportApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `submitIrregularityReport posts multipart to the documented path`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        submitSample()

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/practitioners/license-irregularity-reports", recorded.path)
        val contentType = recorded.getHeader("Content-Type").orEmpty()
        assertTrue(
            "expected multipart/form-data Content-Type, was: $contentType",
            contentType.startsWith("multipart/form-data; boundary="),
        )
    }

    @Test
    fun `submitIrregularityReport includes every documented text part by name`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        submitSample()

        val body = server.takeRequest().body.readUtf8()
        // Each text part MUST appear with its documented snake_case name —
        // renaming any of these client-side would silently drop that field
        // on the server.
        listOf(
            "name=\"name_on_card\"",
            "name=\"license_number\"",
            "name=\"cadre\"",
            "name=\"gender\"",
            "name=\"remark\"",
            "name=\"reported_at\"",
        ).forEach { partName ->
            assertTrue(
                "expected multipart part header containing [$partName], body was:\n$body",
                body.contains(partName),
            )
        }
    }

    @Test
    fun `submitIrregularityReport sends the actual text values inside their parts`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        submitSample()

        val body = server.takeRequest().body.readUtf8()
        // Sanity: the values we passed surface in the body next to their headers.
        // The full multipart layout is `header\r\n\r\nvalue\r\n--boundary…`, so
        // a substring check is robust to boundary churn.
        assertTrue(body.contains("Dr. Sarah Jenkins"))
        assertTrue(body.contains("MD-99201-B"))
        assertTrue(body.contains("Medical Doctor"))
        assertTrue(body.contains("Female"))
        assertTrue(body.contains("Card photo does not match the named practitioner."))
        assertTrue(body.contains("1768515600000"))
    }

    @Test
    fun `submitIrregularityReport includes the snapshot part with filename and image content-type`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":null}"""))

        submitSample()

        val body = server.takeRequest().body.readUtf8()
        assertTrue(
            "expected snapshot part with form-data name + filename, body was:\n$body",
            body.contains("name=\"snapshot\"") && body.contains("filename=\"evidence.jpg\""),
        )
        assertTrue(
            "expected snapshot part Content-Type to be image/jpeg, body was:\n$body",
            body.contains("Content-Type: image/jpeg"),
        )
    }

    @Test
    fun `submitIrregularityReport parses success envelope with server id and echoed fields`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "Report submitted.",
                  "data": {
                    "id": 318,
                    "license_number": "MD-99201-B",
                    "remark": "Card photo does not match the named practitioner."
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = submitSample()

        assertTrue(response.isSuccessful)
        val data = response.body()!!.data!!
        assertEquals(318L, data.id)
        assertEquals("MD-99201-B", data.license_number)
        assertEquals("Card photo does not match the named practitioner.", data.remark)
    }

    // region helpers

    private suspend fun submitSample() = api.submitIrregularityReport(
        nameOnCard = textPart("Dr. Sarah Jenkins"),
        licenseNumber = textPart("MD-99201-B"),
        cadre = textPart("Medical Doctor"),
        gender = textPart("Female"),
        remark = textPart("Card photo does not match the named practitioner."),
        reportedAt = textPart("1768515600000"),
        snapshot = MultipartBody.Part.createFormData(
            name = "snapshot",
            filename = "evidence.jpg",
            body = byteArrayOf(
                0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), // JPEG SOI + APP0
            ).toRequestBody("image/jpeg".toMediaTypeOrNull()),
        ),
    )

    private fun textPart(value: String) =
        value.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

    // endregion

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
