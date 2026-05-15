package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncExamRecordsUseCaseTest {

    private val repository = mockk<ExamSyncRepository>()
    private val useCase = SyncExamRecordsUseCase(repository)

    @Test
    fun `forwards batch counter verbatim`() = runTest {
        val expected = SyncBatchResult(
            attempted = 10,
            succeeded = 8,
            failed = 2,
            errors = listOf("offline", "401"),
        )
        coEvery { repository.syncPending() } returns expected

        assertEquals(expected, useCase())
    }

    @Test
    fun `empty batch passes through`() = runTest {
        coEvery { repository.syncPending() } returns SyncBatchResult.Empty

        assertEquals(SyncBatchResult.Empty, useCase())
    }
}
