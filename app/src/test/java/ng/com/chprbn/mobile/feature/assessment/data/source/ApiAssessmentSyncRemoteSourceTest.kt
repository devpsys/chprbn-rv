package ng.com.chprbn.mobile.feature.assessment.data.source

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ScorePushResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiAssessmentSyncRemoteSourceTest {

    private val api = mockk<AssessmentSyncApiService>()
    private val source = ApiAssessmentSyncRemoteSource(api)

    @Test
    fun `successful batch marks every well-formed row Success`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.success(ScorePushResponseDto(status = true, data = listOf(501L)))

        val results = source.uploadPracticalScoreBatch(listOf(row("q1"), row("q2")))

        assertTrue(results.getValue("8:101:8-sec-1-q1").isSuccess)
        assertTrue(results.getValue("8:101:8-sec-1-q2").isSuccess)
    }

    @Test
    fun `single HTTP call carries practicals object with empty projects`() = runTest {
        val captured = slot<PracticalPushRequestDto>()
        coEvery { api.uploadPracticalScoreBatch(capture(captured)) } returns
            Response.success(ScorePushResponseDto(status = true))

        source.uploadPracticalScoreBatch(listOf(row("q1"), row("q2")))

        assertEquals(2, captured.captured.practicals.size)
        assertTrue(captured.captured.projects.isEmpty())
        assertEquals(listOf(1L, 2L), captured.captured.practicals.map { it.questionId })
    }

    @Test
    fun `server status false fails every well-formed row with the server message`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.success(ScorePushResponseDto(status = false, message = "duplicate submission"))

        val results = source.uploadPracticalScoreBatch(listOf(row("q1"), row("q2")))

        assertEquals("duplicate submission", results.getValue("8:101:8-sec-1-q1").exceptionOrNull()!!.message)
        assertEquals("duplicate submission", results.getValue("8:101:8-sec-1-q2").exceptionOrNull()!!.message)
    }

    @Test
    fun `non-numeric ids fail that row without an HTTP call for an empty remainder`() = runTest {
        val results = source.uploadPracticalScoreBatch(
            listOf(
                PracticalScoreUploadRow(
                    clientId = "bad",
                    paperId = "PE-2024",
                    candidateId = "c1",
                    questionId = "q-x",
                    scheduledCandidateId = "x",
                    scheduleId = "y",
                    score = 8,
                ),
            ),
        )

        assertTrue(results.getValue("bad").isFailure)
        assertTrue(
            results.getValue("bad").exceptionOrNull()!!.message!!.contains("non-numeric"),
        )
    }

    @Test
    fun `non-2xx transport failure maps every well-formed row to Result failure`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val results = source.uploadPracticalScoreBatch(listOf(row("q1"), row("q2")))

        assertTrue(results.getValue("8:101:8-sec-1-q1").isFailure)
        assertTrue(results.getValue("8:101:8-sec-1-q1").exceptionOrNull()!!.message!!.contains("500"))
    }

    @Test
    fun `IOException transport failure maps every well-formed row to Result failure with the cause`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } throws IOException("offline")

        val results = source.uploadPracticalScoreBatch(listOf(row("q1")))

        assertTrue(results.getValue("8:101:8-sec-1-q1").exceptionOrNull() is IOException)
    }

    @Test
    fun `empty input returns empty map without HTTP call`() = runTest {
        assertTrue(source.uploadPracticalScoreBatch(emptyList()).isEmpty())
    }

    private fun row(questionSuffix: String) = PracticalScoreUploadRow(
        clientId = "8:101:8-sec-1-$questionSuffix",
        paperId = "8",
        candidateId = "101",
        questionId = "8-sec-1-$questionSuffix",
        scheduledCandidateId = "501",
        scheduleId = "45",
        score = 8,
    )
}
