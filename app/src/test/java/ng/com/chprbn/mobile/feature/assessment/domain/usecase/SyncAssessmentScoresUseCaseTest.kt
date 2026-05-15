package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentSyncRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncAssessmentScoresUseCaseTest {

    private val repository = mockk<AssessmentSyncRepository>()
    private val useCase = SyncAssessmentScoresUseCase(repository)

    @Test
    fun `delegates to repository and returns batch result`() = runTest {
        val expected = SyncBatchResult(
            attempted = 6,
            succeeded = 5,
            failed = 1,
            errors = listOf("row C-001/q3: timeout"),
        )
        coEvery { repository.syncPending() } returns expected

        val result = useCase()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.syncPending() }
    }

    @Test
    fun `empty batch result propagates`() = runTest {
        coEvery { repository.syncPending() } returns SyncBatchResult.Empty

        val result = useCase()

        assertEquals(SyncBatchResult.Empty, result)
    }
}
