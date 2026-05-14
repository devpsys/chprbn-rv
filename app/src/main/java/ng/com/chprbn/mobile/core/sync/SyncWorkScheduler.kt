package ng.com.chprbn.mobile.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around WorkManager used by every feature repository after a
 * local write. Keeps the WorkManager-specific knobs (constraints, backoff,
 * unique-work policy) in one place so the schedule call sites are one-liners.
 *
 * The unique-work name `sync-upload` collapses concurrent enqueues so a burst
 * of 30 attendance writes triggers exactly one worker run, not 30. The
 * `KEEP` policy preserves an in-flight run rather than restarting it.
 */
@Singleton
class SyncWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_INITIAL_DELAY_SECONDS,
                TimeUnit.SECONDS,
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "sync-upload"
        private const val BACKOFF_INITIAL_DELAY_SECONDS = 30L
    }
}
