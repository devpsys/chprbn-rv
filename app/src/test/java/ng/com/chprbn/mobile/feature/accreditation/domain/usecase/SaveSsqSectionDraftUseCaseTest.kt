package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveSsqSectionDraftUseCaseTest {

    private val repository = mockk<AssessmentDraftRepository>()
    private val useCase = SaveSsqSectionDraftUseCase(repository)

    @Test
    fun `invoke resolves current draft then saves section`() = runTest {
        coEvery { repository.getOrCreateCurrent("inst-abuth") } returns AssessmentDraftRecord(
            id = "assess-inst-abuth",
            institutionId = "inst-abuth",
            institutionName = "ABUTH",
            cycleLabel = "Cycle 2024/2025",
            facilityCode = "ABUTH-ZR-049",
            locationLabel = "Zaria",
            status = DraftPersistenceStatus.SavedLocally,
            updatedAt = 1L,
        )
        coEvery {
            repository.saveSection(
                assessmentId = "assess-inst-abuth",
                sectionKey = SsqSectionKey.A,
                payloadJson = """{"ok":true}""",
                status = DraftPersistenceStatus.SavedLocally,
                itemsCompleted = 24,
                totalItems = 24,
            )
        } returns DraftOpResult.Success

        val result = useCase(
            institutionId = "inst-abuth",
            sectionKey = SsqSectionKey.A,
            payloadJson = """{"ok":true}""",
            itemsCompleted = 24,
            totalItems = 24,
        )

        assertTrue(result is DraftOpResult.Success)
        coVerify(exactly = 1) { repository.getOrCreateCurrent("inst-abuth") }
        coVerify(exactly = 1) {
            repository.saveSection(
                "assess-inst-abuth",
                SsqSectionKey.A,
                """{"ok":true}""",
                DraftPersistenceStatus.SavedLocally,
                24,
                24,
            )
        }
        assertEquals(DraftOpResult.Success, result)
    }
}
