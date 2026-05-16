package ng.com.chprbn.mobile.feature.exam.data.source

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchResultsDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncResultDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiExamSyncRemoteSourceTest {

    private val api = mockk<ExamSyncApiService>()
    private val source = ApiExamSyncRemoteSource(api)

    @Test
    fun `successful attendance batch returns Result success`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(
                AttendanceSyncBatchEnvelopeDto(
                    success = true,
                    data = AttendanceSyncBatchResultsDto(
                        results = listOf(
                            AttendanceSyncResultDto(
                                clientId = "p1:c1",
                                accepted = true,
                                serverId = "srv-1",
                            )
                        )
                    )
                )
            )

        val result = source.uploadAttendance(attendance())

        assertTrue("expected success but was $result", result.isSuccess)
    }

    @Test
    fun `single-item batch wraps domain row into items of size 1`() = runTest {
        val captured = slot<AttendanceSyncBatchRequestDto>()
        coEvery { api.uploadAttendanceBatch(capture(captured)) } returns
            Response.success(AttendanceSyncBatchEnvelopeDto(success = true))

        source.uploadAttendance(attendance())

        assertEquals(1, captured.captured.items.size)
        assertEquals("p1:c1", captured.captured.items.first().clientId)
        assertEquals("signed_in", captured.captured.items.first().status)
    }

    @Test
    fun `per-row reject in batch result returns Result failure with error message`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(
                AttendanceSyncBatchEnvelopeDto(
                    success = true,
                    data = AttendanceSyncBatchResultsDto(
                        results = listOf(
                            AttendanceSyncResultDto(
                                clientId = "p1:c1",
                                accepted = false,
                                error = "candidate not assigned to paper",
                            )
                        )
                    )
                )
            )

        val result = source.uploadAttendance(attendance())

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("expected per-row error message, was: $msg", msg.contains("candidate not assigned"))
    }

    @Test
    fun `non-2xx response returns Result failure with status code in message`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val result = source.uploadAttendance(attendance())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("500"))
    }

    @Test
    fun `IOException from Retrofit returns Result failure`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } throws IOException("offline")

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
