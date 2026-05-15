package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRemarksForCandidateUseCaseTest {

    private val repository = mockk<RemarkRepository>()
    private val useCase = GetRemarksForCandidateUseCase(repository)

    @Test
    fun `blank id returns empty list without touching repository`() = runTest {
        val result = useCase("   ")

        assertEquals(emptyList<Remark>(), result)
        coVerify(exactly = 0) { repository.getRemarksForCandidate(any()) }
    }

    @Test
    fun `trims id and forwards to repository`() = runTest {
        val remarks = listOf(
            Remark(id = "r1", candidateId = "c1", body = "Late", createdAt = 0L),
        )
        coEvery { repository.getRemarksForCandidate("c1") } returns remarks

        val result = useCase("  c1 ")

        assertEquals(remarks, result)
        coVerify(exactly = 1) { repository.getRemarksForCandidate("c1") }
    }
}
