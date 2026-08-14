package ng.com.chprbn.mobile.feature.exam.data.api

import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire regression test pinned against the *actual* production
 * response for `GET attendance/fetch-record` (captured from a live
 * device). The live backend sends `data.centre` (British spelling) and
 * `status` instead of `success` — both diverge from what
 * `full-api-documentation.md` §8.1 originally speculated, and the
 * `centre`/`center` mismatch caused a hard "no dossier" failure until
 * this was caught. `schedules`/`year`/`sections` are real fields on the
 * wire that the client does not parse yet — not a regression target
 * here, just documented so the gap is visible.
 */
class ExamDossierApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ExamDossierApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, ExamDossierApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses the live centre payload despite the centre-vs-center and status-vs-success spelling mismatches`() = runTest {
        // Captured verbatim from a real device response, 2026-08-14.
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(
                    """
                    {
                      "status": true,
                      "message": "Successful",
                      "data": {
                        "centre": {
                          "id": 229,
                          "name": "213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY",
                          "location": "213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY",
                          "status": "Active",
                          "api_key": "mchst213",
                          "sample_token": null
                        },
                        "schedules": [
                          {
                            "id": 1,
                            "test_date": "Friday, 14th Aug 2026",
                            "test_code": "CHEW",
                            "test_type": "Regular Exam",
                            "paper_candidates": [],
                            "candidates": []
                          }
                        ],
                        "papers": [],
                        "year": "2026",
                        "sections": []
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.fetchDossier()

        assertTrue(response.isSuccessful)
        val envelope = response.body()
        assertNotNull(envelope)
        assertTrue("success should read true via the status alternate", envelope!!.success)

        val center = envelope.data?.center
        assertNotNull("data.centre must map onto ExamDossierDataDto.center", center)
        assertEquals("229", center!!.id)
        assertEquals("213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY", center.name)
        assertEquals("213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY", center.location)
    }
}
