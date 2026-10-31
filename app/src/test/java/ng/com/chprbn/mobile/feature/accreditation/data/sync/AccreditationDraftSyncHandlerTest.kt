package ng.com.chprbn.mobile.feature.accreditation.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftEntity
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
    private val remote = mockk<AccreditationSyncRemoteSource>()
    private val handler = AccreditationDraftSyncHandler(draftDao, remote, clock)

    @Test
    fun `uploadBatch marks Synced and returns Success when Fake upload ok`() = runTest {
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
        coEvery { remote.upload("assess-1") } returns true

        val outcomes = handler.uploadBatch(listOf("assess-1"))

        assertEquals(SyncOutcome.Success, outcomes["assess-1"])
        coVerify {
            draftDao.updateStatus("assess-1", DraftPersistenceStatus.Synced.name, now)
        }
    }

    @Test
    fun `uploadBatch drops ghost jobs when draft missing`() = runTest {
        coEvery { draftDao.getById("ghost") } returns null

        val outcomes = handler.uploadBatch(listOf("ghost"))

        assertTrue(outcomes["ghost"] is SyncOutcome.Drop)
        coVerify(exactly = 0) { remote.upload(any()) }
    }
}
