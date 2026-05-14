package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
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
) : AssessmentSyncRepository {

    override suspend fun syncPending(): SyncBatchResult = withContext(Dispatchers.IO) {
        runner.runBatch()
    }
}
