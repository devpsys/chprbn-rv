package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetail
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExamPaperDetailUseCaseTest {

    private val repository = mockk<ExamPaperRepository>()
    private val useCase = GetExamPaperDetailUseCase(repository)

    @Test
    fun `rejects blank paper id without touching repository`() = runTest {
        val result = useCase("   ")

        assertEquals(
            "Paper id is required.",
            (result as ExamPaperDetailResult.Error).message,
        )
        coVerify(exactly = 0) { repository.getPaperDetail(any()) }
    }

    @Test
    fun `trims paper id and forwards to repository`() = runTest {
        val expected = ExamPaperDetailResult.Success(
            ExamPaperDetail(
                paper = Paper(
                    id = "p1",
                    centerId = "c1",
                    title = "Paper 1",
                    subtitle = "Practical",
                    paperKind = PaperKind.Practical,
                    startAt = 0L,
                    endAt = 1L,
                    hall = "Hall A",
                    totalCandidates = 30,
                ),
                center = Center("c1", "Lagos Centre", "LAG", "Lagos"),
                totalCandidates = 30,
                checkedInCount = 10,
                lastSyncAt = null,
                pendingSyncCount = 0,
            ),
        )
        coEvery { repository.getPaperDetail("p1") } returns expected

        val result = useCase("  p1  ")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getPaperDetail("p1") }
    }

    @Test
    fun `propagates NotFound arm verbatim`() = runTest {
        coEvery { repository.getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound

        assertEquals(ExamPaperDetailResult.NotFound, useCase("p1"))
    }
}
