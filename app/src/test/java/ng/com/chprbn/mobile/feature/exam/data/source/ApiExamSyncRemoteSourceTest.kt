package ng.com.chprbn.mobile.feature.exam.data.source

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiExamSyncRemoteSourceTest {

    private val api = mockk<ExamSyncApiService>()
    private val source = ApiExamSyncRemoteSource(api)

    @Test
    fun `successful attendance upload returns Result success`() = runTest {
        coEvery { api.uploadAttendance(any()) } returns
            Response.success(AttendanceSyncEnvelopeDto(status = true))

        val result = source.uploadAttendance(attendance())

        assertTrue("expected success but was $result", result.isSuccess)
    }

    @Test
    fun `non-2xx response returns Result failure with status code in message`() = runTest {
        coEvery { api.uploadAttendance(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val result = source.uploadAttendance(attendance())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("500"))
    }

    @Test
    fun `IOException from Retrofit returns Result failure`() = runTest {
        coEvery { api.uploadAttendance(any()) } throws IOException("offline")

        val result = source.uploadAttendance(attendance())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    private fun attendance() = Attendance(
        paperId = "p1",
        candidateId = "c1",
        status = AttendanceStatus.SignedIn,
        markedAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending,
    )
}
