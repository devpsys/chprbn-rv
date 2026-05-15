package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ClearExamCacheUseCaseTest {

    private val repository = mockk<ExamStatisticsRepository>()
    private val useCase = ClearExamCacheUseCase(repository)

    @Test
    fun `forwards Success arm verbatim`() = runTest {
        coEvery { repository.clearLocalCache() } returns SaveResult.Success

        assertEquals(SaveResult.Success, useCase())
        coVerify(exactly = 1) { repository.clearLocalCache() }
    }

    @Test
    fun `forwards Error arm verbatim`() = runTest {
        val expected = SaveResult.Error("disk full")
        coEvery { repository.clearLocalCache() } returns expected

        assertEquals(expected, useCase())
    }
}
