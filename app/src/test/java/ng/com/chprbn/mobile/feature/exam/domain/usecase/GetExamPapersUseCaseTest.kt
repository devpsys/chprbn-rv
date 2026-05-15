package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExamPapersUseCaseTest {

    private val repository = mockk<ExamPaperRepository>()
    private val useCase = GetExamPapersUseCase(repository)

    @Test
    fun `forwards repository list verbatim`() = runTest {
        val papers = listOf(
            Paper(
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
        )
        coEvery { repository.getPapersForToday() } returns papers

        assertEquals(papers, useCase())
    }

    @Test
    fun `empty list passes through unchanged`() = runTest {
        coEvery { repository.getPapersForToday() } returns emptyList()

        assertEquals(emptyList<Paper>(), useCase())
    }
}
