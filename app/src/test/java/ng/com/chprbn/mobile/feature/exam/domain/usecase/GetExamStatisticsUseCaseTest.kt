package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import org.junit.Assert.assertSame
import org.junit.Test

class GetExamStatisticsUseCaseTest {

    private val repository = mockk<ExamStatisticsRepository>()
    private val useCase = GetExamStatisticsUseCase(repository)

    @Test
    fun `returns aggregated statistics from repository`() = runTest {
        val stats = ExamStatistics(
            recordsDownloaded = 100,
            attendanceCaptured = 60,
            syncedCount = 50,
            cachedCount = 100,
            pendingCount = 10,
            failedCount = 0,
            lastUpdatedAt = 1_700_000_000_000L,
        )
        coEvery { repository.getStatistics() } returns stats

        assertSame(stats, useCase())
    }
}
