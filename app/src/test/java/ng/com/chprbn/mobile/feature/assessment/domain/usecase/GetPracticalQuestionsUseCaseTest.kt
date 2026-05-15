package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPracticalQuestionsUseCaseTest {

    private val repository = mockk<PracticalScoringRepository>()
    private val useCase = GetPracticalQuestionsUseCase(repository)

    @Test
    fun `any blank input short-circuits to empty list`() = runTest {
        assertTrue(useCase("", "C1", "A").isEmpty())
        assertTrue(useCase("S1", "  ", "A").isEmpty())
        assertTrue(useCase("S1", "C1", "").isEmpty())
        coVerify(exactly = 0) { repository.getQuestions(any(), any(), any()) }
    }

    @Test
    fun `trimmed inputs are forwarded - pairs returned`() = runTest {
        val q1 = SectionQuestion(
            id = "q1",
            sectionId = "A",
            number = 1,
            prompt = "Measures BP",
            maxScore = 5,
        )
        val score = PracticalScore(
            scheduleId = "PE",
            candidateId = "C1",
            questionId = "q1",
            score = 4,
            scoredAt = 0L,
        )
        coEvery {
            repository.getQuestions("PE", "C1", "A")
        } returns listOf(q1 to score, q1.copy(id = "q2", number = 2) to null)

        val result = useCase(" PE ", "C1 ", " A")

        assertEquals(2, result.size)
        assertEquals(score, result[0].second)
        assertEquals(null, result[1].second)
        coVerify(exactly = 1) { repository.getQuestions("PE", "C1", "A") }
    }
}
