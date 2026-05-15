package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadExamDossierUseCaseTest {

    private val repository = mockk<ExamSyncRepository>()
    private val useCase = DownloadExamDossierUseCase(repository)

    @Test
    fun `forwards Success arm with counters`() = runTest {
        val expected = DownloadDossierResult.Success(
            papersCount = 5,
            candidatesCount = 200,
        )
        coEvery { repository.downloadDossier() } returns expected

        assertEquals(expected, useCase())
    }

    @Test
    fun `forwards Error arm verbatim`() = runTest {
        val expected = DownloadDossierResult.Error("offline")
        coEvery { repository.downloadDossier() } returns expected

        assertEquals(expected, useCase())
    }
}
