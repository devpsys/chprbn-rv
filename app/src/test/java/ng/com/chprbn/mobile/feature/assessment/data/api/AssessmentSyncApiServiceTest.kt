package ng.com.chprbn.mobile.feature.assessment.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectPushItemDto
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssessmentSyncApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AssessmentSyncApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, AssessmentSyncApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `uploadPracticalScoreBatch posts an object with practicals and empty projects`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"Successful","data":[501]}"""))

        api.uploadPracticalScoreBatch(
            PracticalPushRequestDto(
                practicals = listOf(
                    PracticalPushItemDto(
                        scheduledCandidateId = 501,
                        candidateId = 101,
                        paperId = 8,
                        questionId = 11,
                        scheduleId = 45,
                        score = 8.0,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/practical/push-record", recorded.path)

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("practicals").single().asJsonObject
        assertEquals(501L, item["scheduled_candidate_id"].asLong)
        assertEquals(101L, item["candidate_id"].asLong)
        assertEquals(8L, item["paper_id"].asLong)
        assertEquals(11L, item["question_id"].asLong)
        assertEquals(45L, item["schedule_id"].asLong)
        assertEquals(8.0, item["score"].asDouble, 0.0)
        assertEquals(0, body.getAsJsonArray("projects").size())
    }

    @Test
    fun `uploadPracticalScoreBatch parses status envelope`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"Successful","data":[501]}"""))

        val response = api.uploadPracticalScoreBatch(
            PracticalPushRequestDto(practicals = emptyList()),
        )

        assertTrue(response.isSuccessful)
        assertTrue(response.body()!!.status)
        assertEquals(listOf(501L), response.body()!!.data)
    }

    @Test
    fun `uploadPracticalScoreBatch 500 surfaces as non-successful response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val response = api.uploadPracticalScoreBatch(
            PracticalPushRequestDto(
                practicals = listOf(
                    PracticalPushItemDto(501, 101, 8, 11, 45, 8.0),
                ),
            ),
        )

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    @Test
    fun `uploadProjectScoreBatch posts a top-level JSON array`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"Successful","data":[501]}"""))

        api.uploadProjectScoreBatch(
            listOf(
                ProjectPushItemDto(
                    scheduledCandidateId = 501,
                    scheduleId = 45,
                    candidateId = 101,
                    paperId = 9,
                    score = 72.5,
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/project/push-record", recorded.path)

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonArray
        val item = body.single().asJsonObject
        assertEquals(501L, item["scheduled_candidate_id"].asLong)
        assertEquals(45L, item["schedule_id"].asLong)
        assertEquals(101L, item["candidate_id"].asLong)
        assertEquals(9L, item["paper_id"].asLong)
        assertEquals(72.5, item["score"].asDouble, 0.0)
    }

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
}
