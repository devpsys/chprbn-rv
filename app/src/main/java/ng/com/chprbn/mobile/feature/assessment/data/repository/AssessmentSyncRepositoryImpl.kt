package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSyncStats
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentSyncRepository
import javax.inject.Inject

/**
 * The user-initiated "Sync Now" gesture. Runs one cross-feature batch
 * through [SyncBatchRunner] and surfaces the counter back to the UI.
 *
 * The batch is global (not assessment-scoped) because the queue is
 * shared — running it here flushes exam-side rows too, which is fine:
 * the user wouldn't want to sit through two separate sync buttons.
 */
class AssessmentSyncRepositoryImpl @Inject constructor(
    private val runner: SyncBatchRunner,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
) : AssessmentSyncRepository {

    override suspend fun syncPending(): SyncBatchResult = withContext(Dispatchers.IO) {
        runner.runBatch()
    }

    override suspend fun getSyncStats(scheduleId: String): AssessmentSyncStats =
        withContext(Dispatchers.IO) {
            val pending = practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name) +
                practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name) +
                projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name) +
                projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name)
            val lastPractical = practicalScoreDao.mostRecentSyncedAt(scheduleId)
            val lastProject = projectScoreDao.mostRecentSyncedAt(scheduleId)
            AssessmentSyncStats(
                pendingSyncCount = pending,
                lastSyncAt = listOfNotNull(lastPractical, lastProject).maxOrNull(),
            )
        }
}
