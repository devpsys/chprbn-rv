package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAssessmentCandidatesUseCaseTest {

    private val repository = mockk<AssessmentCandidateRepository>()
    private val useCase = GetAssessmentCandidatesUseCase(repository)

    @Test
    fun `blank schedule returns empty list without hitting repository`() = runTest {
        val result = useCase(scheduleId = "  ")

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { repository.getCandidates(any(), any()) }
    }

    @Test
    fun `trimmed schedule and query are forwarded to repository`() = runTest {
        val row = AssessmentCandidateRow(
            candidate = Candidate("c1", "EX-001", "Jane Doe"),
            aggregateScore = 42,
            level = ScoreLevel.Normal,
            scoredQuestions = 6,
            totalQuestions = 10,
            syncStatus = SyncStatus.Pending,
        )
        coEvery {
            repository.getCandidates("PE-2024", "jane")
        } returns listOf(row)

        val result = useCase(
            scheduleId = " PE-2024 ",
            query = "  jane  ",
        )

        assertEquals(listOf(row), result)
        coVerify(exactly = 1) { repository.getCandidates("PE-2024", "jane") }
    }

    @Test
    fun `default empty query returns full cohort`() = runTest {
        coEvery { repository.getCandidates("PE-2024", "") } returns emptyList()

        useCase(scheduleId = "PE-2024")

        coVerify(exactly = 1) { repository.getCandidates("PE-2024", "") }
    }
}
