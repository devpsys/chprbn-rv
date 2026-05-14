package ng.com.chprbn.mobile.core.sync

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure-Kotlin core of the sync engine, factored out of [SyncWorker] so the
 * dispatch logic is unit-testable without WorkManager.
 *
 * Pulls up to [batchSize] pending/failed rows, dispatches each to the matching
 * [SyncEntityHandler] via the injected multibinding map, and writes the
 * per-row outcome back to the queue. Per-row failures do not abort the batch
 * — partial success is the normal case.
 *
 * [clock] is injectable so tests can pin time. In production it's bound to
 * [Clock.System].
 */
@Singleton
class SyncBatchRunner @Inject constructor(
    private val syncJobDao: SyncJobDao,
    private val handlers: Map<SyncEntityType, @JvmSuppressWildcards SyncEntityHandler>,
    private val clock: Clock,
) {

    suspend fun runBatch(batchSize: Int = DEFAULT_BATCH_SIZE): SyncBatchResult {
        val jobs = syncJobDao.pendingAndFailed(limit = batchSize)
        if (jobs.isEmpty()) return SyncBatchResult.Empty

        var succeeded = 0
        var failed = 0
        val errors = mutableListOf<String>()

        for (job in jobs) {
            val type = runCatching { SyncEntityType.valueOf(job.entityType) }.getOrNull()
            val handler = type?.let { handlers[it] }
            val outcome: SyncOutcome = when {
                type == null -> SyncOutcome.Failure("Unknown entity type: ${job.entityType}")
                handler == null -> SyncOutcome.Failure("No handler bound for $type")
                else -> runCatching { handler.upload(job.entityKey) }
                    .getOrElse { t -> SyncOutcome.Failure(t.message ?: "Unknown error") }
            }

            val attemptedAt = clock.nowMillis()
            when (outcome) {
                is SyncOutcome.Success -> {
                    syncJobDao.delete(job.id)
                    succeeded++
                }
                is SyncOutcome.Failure -> {
                    syncJobDao.markAttempted(
                        id = job.id,
                        status = SyncStatus.Failed.name,
                        attemptedAt = attemptedAt,
                        error = outcome.message,
                    )
                    failed++
                    errors.add(outcome.message)
                }
            }
        }

        return SyncBatchResult(
            attempted = jobs.size,
            succeeded = succeeded,
            failed = failed,
            errors = errors.toList(),
        )
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 50
    }
}
