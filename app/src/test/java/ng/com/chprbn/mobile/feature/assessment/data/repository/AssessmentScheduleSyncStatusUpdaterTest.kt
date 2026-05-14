package ng.com.chprbn.mobile.feature.assessment.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentScheduleDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import org.junit.Test

/**
 * The schedule's derived [SyncStatus] follows priority
 * `Failed > Pending > Synced`. The updater short-circuits as soon as any
 * failed row is detected — we verify that by asserting pending counts
 * aren't even queried in the Failed case.
 */
class AssessmentScheduleSyncStatusUpdaterTest {

    private val scheduleDao = mockk<AssessmentScheduleDao>(relaxUnitFun = true) {
        coEvery { updateSyncStatus(any(), any()) } returns 1
    }
    private val practicalScoreDao = mockk<PracticalScoreDao>()
    private val projectScoreDao = mockk<ProjectScoreDao>()
    private val updater = AssessmentScheduleSyncStatusUpdater(
        scheduleDao, practicalScoreDao, projectScoreDao,
    )

    @Test
    fun `failed row across either table marks schedule Failed`() = runTest {
        coEvery { practicalScoreDao.countByStatusForSchedule("s1", SyncStatus.Failed.name) } returns 0
        coEvery { projectScoreDao.countByStatusForSchedule("s1", SyncStatus.Failed.name) } returns 1

        updater.refresh("s1")

        coVerify(exactly = 1) { scheduleDao.updateSyncStatus("s1", SyncStatus.Failed.name) }
        // Short-circuit: pending counts should not have been queried.
        coVerify(exactly = 0) {
            practicalScoreDao.countByStatusForSchedule("s1", SyncStatus.Pending.name)
        }
    }

    @Test
    fun `pending row with no failed marks schedule Pending`() = runTest {
        coEvery { practicalScoreDao.countByStatusForSchedule("s1", SyncStatus.Failed.name) } returns 0
        coEvery { projectScoreDao.countByStatusForSchedule("s1", SyncStatus.Failed.name) } returns 0
        coEvery { practicalScoreDao.countByStatusForSchedule("s1", SyncStatus.Pending.name) } returns 3
        coEvery { projectScoreDao.countByStatusForSchedule("s1", SyncStatus.Pending.name) } returns 0

        updater.refresh("s1")

        coVerify(exactly = 1) { scheduleDao.updateSyncStatus("s1", SyncStatus.Pending.name) }
    }

    @Test
    fun `no failed or pending rows marks schedule Synced`() = runTest {
        coEvery { practicalScoreDao.countByStatusForSchedule(any(), any()) } returns 0
        coEvery { projectScoreDao.countByStatusForSchedule(any(), any()) } returns 0

        updater.refresh("s1")

        coVerify(exactly = 1) { scheduleDao.updateSyncStatus("s1", SyncStatus.Synced.name) }
    }
}
