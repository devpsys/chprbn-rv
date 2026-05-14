package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkAttendanceUseCaseTest {

    private val repository = mockk<AttendanceRepository>()
    private val useCase = MarkAttendanceUseCase(repository)

    @Test
    fun `rejects blank paperId without touching repository`() = runTest {
        val result = useCase(paperId = "  ", candidateId = "c1", status = AttendanceStatus.SignedIn)

        assertEquals("Paper and candidate are required.", (result as MarkAttendanceResult.Error).message)
        coVerify(exactly = 0) { repository.markAttendance(any(), any(), any()) }
    }

    @Test
    fun `rejects blank candidateId`() = runTest {
        val result = useCase(paperId = "p1", candidateId = "", status = AttendanceStatus.SignedIn)

        assertTrue(result is MarkAttendanceResult.Error)
    }

    @Test
    fun `trims ids and forwards to repository`() = runTest {
        val expected = MarkAttendanceResult.Success(
            Attendance(
                paperId = "p1",
                candidateId = "c1",
                status = AttendanceStatus.SignedIn,
                markedAt = 0L,
            ),
        )
        coEvery {
            repository.markAttendance("p1", "c1", AttendanceStatus.SignedIn)
        } returns expected

        val result = useCase(paperId = "  p1 ", candidateId = " c1 ", status = AttendanceStatus.SignedIn)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.markAttendance("p1", "c1", AttendanceStatus.SignedIn) }
    }
}
