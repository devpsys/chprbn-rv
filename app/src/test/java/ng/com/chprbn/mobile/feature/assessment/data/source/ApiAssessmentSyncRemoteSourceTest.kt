package ng.com.chprbn.mobile.feature.assessment.data.source

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiAssessmentSyncRemoteSourceTest {

    private val api = mockk<AssessmentSyncApiService>()
    private val source = ApiAssessmentSyncRemoteSource(api)

    @Test
    fun `successful upload returns Result success`() = runTest {
        coEvery { api.uploadPracticalScore(any()) } returns
            Response.success(PracticalScoreSyncEnvelopeDto(status = true))

        val result = source.uploadPracticalScore(score())

        assertTrue("expected success but was $result", result.isSuccess)
    }

    @Test
    fun `non-2xx response returns Result failure with status detail`() = runTest {
        coEvery { api.uploadPracticalScore(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val result = source.uploadPracticalScore(score())

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("expected HTTP 500 in error message, was: $msg", msg.contains("500"))
    }

    @Test
    fun `IOException from Retrofit returns Result failure`() = runTest {
        coEvery { api.uploadPracticalScore(any()) } throws IOException("offline")

        val result = source.uploadPracticalScore(score())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    private fun score() = PracticalScore(
        scheduleId = "PE-2024",
        candidateId = "c1",
        questionId = "q1",
        score = 8,
        scoredAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending,
    )
}
