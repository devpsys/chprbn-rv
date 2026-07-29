package ng.com.chprbn.mobile.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * WorkManager entry point for the cross-feature upload queue. Hilt-aware via
 * [HiltWorker]; the WorkManager initialiser is wired through
 * `ChprbnApplication`'s `Configuration.Provider`.
 *
 * Scheduling is the [SyncWorkScheduler]'s job — feature repositories never
 * touch WorkManager directly. The worker just runs [SyncBatchRunner] and
 * maps the result:
 *
 * - `failed > 0`  → [Result.retry] (transient failures ride WorkManager's
 *   exponential backoff).
 * - `Pending > 0` remaining after a clean run → [Result.retry] (E5 audit
 *   fix — the batch was capped at [SyncBatchRunner.DEFAULT_BATCH_SIZE] and
 *   there is more to upload; chain a follow-up run instead of stopping).
 * - Otherwise → [Result.success].
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val runner: SyncBatchRunner,
    private val syncJobDao: SyncJobDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val batch = runner.runBatch()
        if (batch.failed > 0) return Result.retry()
        val pendingRemaining = syncJobDao.countByStatus(SyncStatus.Pending.name)
        return if (pendingRemaining > 0) Result.retry() else Result.success()
    }
}
