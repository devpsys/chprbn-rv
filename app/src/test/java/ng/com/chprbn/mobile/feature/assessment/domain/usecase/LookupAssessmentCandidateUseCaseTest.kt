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
        val result = useCase(scheduleId = "  ", scannedPayload = "B2320135")

        assertNull(result)
        coVerify(exactly = 0) { repository.getCandidateByExamNumber(any(), any()) }
    }

    @Test
    fun `blank payload short-circuits to null`() = runTest {
        val result = useCase(scheduleId = "PE", scannedPayload = "")

        assertNull(result)
        coVerify(exactly = 0) { repository.getCandidateByExamNumber(any(), any()) }
    }

    @Test
    fun `trimmed inputs are forwarded as an exam-number lookup`() = runTest {
        val candidate = Candidate("c1", "B2320135", "Jane Doe")
        coEvery { repository.getCandidateByExamNumber("PE", "B2320135") } returns candidate

        val result = useCase(scheduleId = " PE ", scannedPayload = "B2320135 ")

        assertEquals(candidate, result)
        coVerify(exactly = 1) { repository.getCandidateByExamNumber("PE", "B2320135") }
    }

    @Test
    fun `repository miss returns null`() = runTest {
        coEvery { repository.getCandidateByExamNumber("PE", "MISSING") } returns null

        val result = useCase("PE", "MISSING")

        assertNull(result)
    }
}
