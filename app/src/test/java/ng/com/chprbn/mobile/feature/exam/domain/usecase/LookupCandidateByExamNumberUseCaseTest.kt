package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LookupCandidateByExamNumberUseCaseTest {

    private val repository = mockk<ExamCandidateRepository>()
    private val useCase = LookupCandidateByExamNumberUseCase(repository)

    @Test
    fun `blank exam number returns null without touching repository`() = runTest {
        val result = useCase("   ")

        assertNull(result)
        coVerify(exactly = 0) { repository.getCandidateByExamNumber(any()) }
    }

    @Test
    fun `trims exam number and returns repository hit`() = runTest {
        val candidate = Candidate(id = "c1", examNumber = "EX1", fullName = "Ada")
        coEvery { repository.getCandidateByExamNumber("EX1") } returns candidate

        val result = useCase("  EX1 ")

        assertEquals(candidate, result)
        coVerify(exactly = 1) { repository.getCandidateByExamNumber("EX1") }
    }

    @Test
    fun `null repository result propagates`() = runTest {
        coEvery { repository.getCandidateByExamNumber("EX1") } returns null

        assertNull(useCase("EX1"))
    }
}
