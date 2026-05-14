package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddRemarkUseCaseTest {

    private val repository = mockk<RemarkRepository>()
    private val useCase = AddRemarkUseCase(repository)

    @Test
    fun `rejects blank candidate id`() = runTest {
        val result = useCase(candidateId = "", paperId = null, body = "x")

        assertEquals("Candidate is required.", (result as AddRemarkResult.Error).message)
    }

    @Test
    fun `rejects blank body`() = runTest {
        val result = useCase(candidateId = "c1", paperId = null, body = "   ")

        assertEquals("Remark cannot be empty.", (result as AddRemarkResult.Error).message)
    }

    @Test
    fun `trims body and normalises blank paperId to null`() = runTest {
        coEvery {
            repository.addRemark(
                candidateId = "c1",
                paperId = null,
                body = "Late arrival",
                severity = RemarkSeverity.Warning,
            )
        } returns AddRemarkResult.Success(
            Remark(
                id = "r1",
                candidateId = "c1",
                paperId = null,
                body = "Late arrival",
                severity = RemarkSeverity.Warning,
                createdAt = 0L,
            ),
        )

        val result = useCase(
            candidateId = "c1",
            paperId = "   ", // blank → null
            body = "  Late arrival  ",
            severity = RemarkSeverity.Warning,
        )

        assertEquals("Late arrival", (result as AddRemarkResult.Success).remark.body)
        assertNull(result.remark.paperId)
        coVerify(exactly = 1) {
            repository.addRemark("c1", null, "Late arrival", RemarkSeverity.Warning)
        }
    }
}
