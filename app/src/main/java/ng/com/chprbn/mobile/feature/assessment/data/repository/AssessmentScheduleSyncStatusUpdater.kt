package ng.com.chprbn.mobile.feature.assessment.data.repository

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentScheduleDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recomputes a schedule's derived [SyncStatus] from its score-row counts
 * and writes it back to the schedule table. Called after every score
 * write and every sync attempt so the Examination Schedules screen pill
 * reflects current state without a per-row aggregation on read.
 *
 * Priority `Failed > Pending > Synced`:
 * - any row Failed → schedule is Failed
 * - else any row Pending → schedule is Pending
 * - else (no score rows OR all Synced) → schedule is Synced
 *
 * A schedule with no score rows is vacuously `Synced`; the UI suppresses
 * the pill there via row counts on the candidates list.
 */
@Singleton
class AssessmentScheduleSyncStatusUpdater @Inject constructor(
    private val scheduleDao: AssessmentScheduleDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
) {

    suspend fun refresh(scheduleId: String) {
        val failed = practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name) +
            projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name)
        if (failed > 0) {
            scheduleDao.updateSyncStatus(scheduleId, SyncStatus.Failed.name)
            return
        }

        val pending = practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name) +
            projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name)
        val derived = if (pending > 0) SyncStatus.Pending else SyncStatus.Synced
        scheduleDao.updateSyncStatus(scheduleId, derived.name)
    }
}
