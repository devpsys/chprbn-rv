package ng.com.chprbn.mobile.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager entry point for the cross-feature upload queue. Hilt-aware via
 * [HiltWorker]; the WorkManager initialiser is wired through
 * `ChprbnApplication`'s `Configuration.Provider`.
 *
 * Scheduling is the [SyncWorkScheduler]'s job — feature repositories never
 * touch WorkManager directly. The worker just runs [SyncBatchRunner] and maps
 * its result to [Result.success] (clean) / [Result.retry] (anything failed,
 * lets WorkManager apply exponential backoff).
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val runner: SyncBatchRunner,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val batch = runner.runBatch()
        return if (batch.failed == 0) Result.success() else Result.retry()
    }
}
