package ng.com.chprbn.mobile.feature.accreditation.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftSyncPayload
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccreditationDraftSyncHandlerTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val draftDao = mockk<AssessmentDraftDao> {
        coEvery { updateStatus(any(), any(), any()) } returns 1
    }
    private val sectionDao = mockk<SsqSectionDraftDao>()
    private val criteriaDao = mockk<CriteriaDraftDao>()
    private val scoreDao = mockk<ScoreDraftDao>()
    private val attachmentDao = mockk<AccreditationAttachmentDao>()
    private val remote = mockk<AccreditationSyncRemoteSource>()
    private val handler = AccreditationDraftSyncHandler(
        draftDao = draftDao,
        sectionDao = sectionDao,
        criteriaDao = criteriaDao,
        scoreDao = scoreDao,
        attachmentDao = attachmentDao,
        remote = remote,
        clock = clock,
    )

    init {
        coEvery { sectionDao.getForAssessment(any()) } returns emptyList()
        coEvery { criteriaDao.get(any()) } returns null
        coEvery { scoreDao.get(any()) } returns null
        coEvery { attachmentDao.listForAssessment(any()) } returns emptyList()
        coEvery { attachmentDao.updateUploadState(any(), any()) } returns 1
    }

    @Test
    fun `uploadBatch marks Synced and returns Success when upload ok`() = runTest {
        val draft = AssessmentDraftEntity(
            id = "assess-1",
            institutionId = "inst-abuth",
            institutionName = "ABUTH",
            cycleLabel = "Cycle 2024/2025",
            facilityCode = "X",
            locationLabel = "Zaria",
            status = DraftPersistenceStatus.PendingSync.name,
            updatedAt = 0L,
        )
        coEvery { draftDao.getById("assess-1") } returns draft
        val payloadSlot = slot<AccreditationDraftSyncPayload>()
        coEvery { remote.uploadDraft(capture(payloadSlot)) } returns true

        val outcomes = handler.uploadBatch(listOf("assess-1"))

        assertEquals(SyncOutcome.Success, outcomes["assess-1"])
        assertEquals("assess-1", payloadSlot.captured.assessmentId)
        assertEquals("inst-abuth", payloadSlot.captured.institutionId)
        coVerify {
            draftDao.updateStatus("assess-1", DraftPersistenceStatus.Synced.name, now)
        }
    }

    @Test
    fun `uploadBatch drops ghost jobs when draft missing`() = runTest {
        coEvery { draftDao.getById("ghost") } returns null

        val outcomes = handler.uploadBatch(listOf("ghost"))

        assertTrue(outcomes["ghost"] is SyncOutcome.Drop)
        coVerify(exactly = 0) { remote.uploadDraft(any()) }
    }

    @Test
    fun `uploadBatch marks SyncFailed when remote returns false`() = runTest {
        coEvery { draftDao.getById("assess-1") } returns AssessmentDraftEntity(
            id = "assess-1",
            institutionId = "inst-abuth",
            institutionName = "ABUTH",
            cycleLabel = "Cycle 2024/2025",
            facilityCode = "X",
            locationLabel = "Zaria",
            status = DraftPersistenceStatus.PendingSync.name,
            updatedAt = 0L,
        )
        coEvery { remote.uploadDraft(any()) } returns false

        val outcomes = handler.uploadBatch(listOf("assess-1"))

        assertTrue(outcomes["assess-1"] is SyncOutcome.Failure)
        coVerify {
            draftDao.updateStatus("assess-1", DraftPersistenceStatus.SyncFailed.name, now)
        }
    }
}
