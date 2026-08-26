package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.sync.PracticalScoreKey
import ng.com.chprbn.mobile.feature.assessment.data.sync.ProjectScoreKey
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
    private val syncJobDao: SyncJobDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
    private val clock: Clock,
) : AssessmentSyncRepository {

    override suspend fun syncPending(): SyncBatchResult = withContext(Dispatchers.IO) {
        // User-initiated Sync Now — reconcile before batch to heal the
        // two ways rows can end up "stranded" on the Failed tab with
        // no queue entry for the runner to pick up:
        //   (a) a queue row that was auto-flipped to Abandoned after
        //       MAX_ATTEMPTS retries — resetAbandoned=true takes care
        //       of that inside the runner (see SyncBatchRunner);
        //   (b) a feature row that's Pending/Failed but its queue row
        //       has since been deleted — the sweep below re-enqueues
        //       it. The queue's unique index on (entityType, entityKey)
        //       plus OnConflictStrategy.REPLACE makes this idempotent:
        //       existing queue rows are reset to Pending(attempts=0),
        //       missing ones are inserted fresh, so the sweep doubles
        //       as a "retry all failed" for the assessment side.
        reconcileFromScoreTables()
        runner.runBatch(resetAbandoned = true)
    }

    /**
     * Sweeps `practical_scores` and `project_scores` for rows the sync
     * engine still owes and enqueues a fresh queue job for each. Idempotent
     * — see [syncPending] for why.
     */
    private suspend fun reconcileFromScoreTables() {
        val now = clock.nowMillis()
        practicalScoreDao.pendingAndFailed(limit = Int.MAX_VALUE).forEach { row ->
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.PracticalScore.name,
                    entityKey = PracticalScoreKey.encode(
                        scheduleId = row.scheduleId,
                        candidateId = row.candidateId,
                        questionId = row.questionId,
                    ),
                    enqueuedAt = now,
                    status = SyncStatus.Pending.name,
                ),
            )
        }
        projectScoreDao.pendingAndFailed(limit = Int.MAX_VALUE).forEach { row ->
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.ProjectScore.name,
                    entityKey = ProjectScoreKey.encode(
                        scheduleId = row.scheduleId,
                        candidateId = row.candidateId,
                    ),
                    enqueuedAt = now,
                    status = SyncStatus.Pending.name,
                ),
            )
        }
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
