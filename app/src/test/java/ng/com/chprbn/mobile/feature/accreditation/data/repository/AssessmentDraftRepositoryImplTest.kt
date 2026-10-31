package ng.com.chprbn.mobile.feature.accreditation.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.AssessmentSeedBundle
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentDraftRepositoryImplTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val draftDao = mockk<AssessmentDraftDao> {
        coEvery { upsert(any()) } returns 1L
        coEvery { updateStatus(any(), any(), any()) } returns 1
    }
    private val sectionDao = mockk<SsqSectionDraftDao> {
        coEvery { upsert(any()) } returns 1L
        coEvery { upsertAll(any()) } returns listOf(1L)
    }
    private val criteriaDao = mockk<CriteriaDraftDao> {
        coEvery { upsert(any()) } returns 1L
    }
    private val scoreDao = mockk<ScoreDraftDao> {
        coEvery { upsert(any()) } returns 1L
    }
    private val attachmentDao = mockk<AccreditationAttachmentDao>(relaxUnitFun = true)
    private val remote = mockk<AccreditationDraftRemoteSource>()
    private val syncJobDao = mockk<SyncJobDao> {
        coEvery { enqueue(any()) } returns 1L
    }
    private val workScheduler = mockk<SyncWorkScheduler>(relaxUnitFun = true)

    private val repository = AssessmentDraftRepositoryImpl(
        draftDao, sectionDao, criteriaDao, scoreDao, attachmentDao,
        remote, syncJobDao, workScheduler, clock,
    )

    @Test
    fun `getOrCreateCurrent seeds from Fake when Room empty`() = runTest {
        coEvery { draftDao.getByInstitutionId("inst-abuth") } returns null
        val bundle = AssessmentSeedBundle(
            draft = AssessmentDraftRecord(
                id = "assess-inst-abuth",
                institutionId = "inst-abuth",
                institutionName = "ABUTH",
                cycleLabel = "Cycle 2024/2025",
                facilityCode = "ABUTH-ZR-049",
                locationLabel = "Zaria",
                status = DraftPersistenceStatus.SavedLocally,
                updatedAt = now,
            ),
            sections = listOf(
                SsqSectionDraftRecord(
                    assessmentId = "assess-inst-abuth",
                    sectionKey = SsqSectionKey.A,
                    codeLabel = "Section A",
                    rangeLabel = "A1",
                    title = "General",
                    subtitle = "",
                    status = DraftPersistenceStatus.SavedLocally,
                    payloadJson = "",
                    itemsCompleted = 24,
                    totalItems = 24,
                ),
            ),
            criteria = CriteriaDraftRecord(
                "assess-inst-abuth", "", 0, 8, DraftPersistenceStatus.NotStarted,
            ),
            scores = ScoreDraftRecord(
                "assess-inst-abuth", "", 0, 6, DraftPersistenceStatus.NotStarted,
            ),
        )
        coEvery { remote.getSeedBundle("inst-abuth") } returns bundle

        val draft = repository.getOrCreateCurrent("inst-abuth")

        assertEquals("assess-inst-abuth", draft.id)
        coVerify { draftDao.upsert(any()) }
        coVerify { sectionDao.upsertAll(any()) }
        coVerify { criteriaDao.upsert(any()) }
        coVerify { scoreDao.upsert(any()) }
    }

    @Test
    fun `saveSection enqueues AccreditationDraft job and schedules work`() = runTest {
        coEvery { sectionDao.get("assess-1", "A") } returns SsqSectionDraftEntity(
            assessmentId = "assess-1",
            sectionKey = "A",
            codeLabel = "Section A",
            rangeLabel = "A1",
            title = "General",
            subtitle = "",
            status = DraftPersistenceStatus.NotStarted.name,
            payloadJson = "",
            itemsCompleted = 0,
            totalItems = 24,
        )
        val job = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(job)) } returns 1L

        val result = repository.saveSection(
            assessmentId = "assess-1",
            sectionKey = SsqSectionKey.A,
            payloadJson = """{"x":1}""",
            status = DraftPersistenceStatus.SavedLocally,
            itemsCompleted = 24,
            totalItems = 24,
        )

        assertTrue(result is DraftOpResult.Success)
        assertEquals(SyncEntityType.AccreditationDraft.name, job.captured.entityType)
        assertEquals("assess-1", job.captured.entityKey)
        assertEquals(SyncStatus.Pending.name, job.captured.status)
        coVerify(exactly = 1) { workScheduler.scheduleSyncWork() }
    }
}
