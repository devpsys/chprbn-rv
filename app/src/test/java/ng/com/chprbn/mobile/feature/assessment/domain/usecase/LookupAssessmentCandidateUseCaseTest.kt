package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LookupAssessmentCandidateUseCaseTest {

    private val repository = mockk<AssessmentCandidateRepository>()
    private val useCase = LookupAssessmentCandidateUseCase(repository)

    @Test
    fun `blank schedule short-circuits to null`() = runTest {
        val result = useCase(scheduleId = "  ", candidateId = "C1")

        assertNull(result)
        coVerify(exactly = 0) { repository.getCandidate(any(), any()) }
    }

    @Test
    fun `blank candidate short-circuits to null`() = runTest {
        val result = useCase(scheduleId = "PE", candidateId = "")

        assertNull(result)
        coVerify(exactly = 0) { repository.getCandidate(any(), any()) }
    }

    @Test
    fun `trimmed inputs forwarded and candidate returned`() = runTest {
        val candidate = Candidate("c1", "EX-001", "Jane Doe")
        coEvery { repository.getCandidate("PE", "C1") } returns candidate

        val result = useCase(scheduleId = " PE ", candidateId = "C1 ")

        assertEquals(candidate, result)
        coVerify(exactly = 1) { repository.getCandidate("PE", "C1") }
    }

    @Test
    fun `repository miss returns null`() = runTest {
        coEvery { repository.getCandidate("PE", "MISSING") } returns null

        val result = useCase("PE", "MISSING")

        assertNull(result)
    }
}
