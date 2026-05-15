package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamTaskSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.OfficerSession
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GetExamDashboardUseCaseTest {

    private val repository = mockk<ExamPaperRepository>()
    private val useCase = GetExamDashboardUseCase(repository)

    @Test
    fun `forwards repository result verbatim`() = runTest {
        val expected = ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Lagos Centre", "LAG", "Lagos"),
                attendanceCard = ExamTaskSummary("Active Session", "120 candidates"),
                practicalCard = ExamTaskSummary("Pending Grading", "30 candidates"),
            ),
        )
        coEvery { repository.getDashboardSummary() } returns expected

        val result = useCase()

        assertSame(expected, result)
    }

    @Test
    fun `propagates Error arm without rewriting message`() = runTest {
        val expected = ExamDashboardResult.Error("offline")
        coEvery { repository.getDashboardSummary() } returns expected

        val result = useCase()

        assertEquals(expected, result)
    }
}
