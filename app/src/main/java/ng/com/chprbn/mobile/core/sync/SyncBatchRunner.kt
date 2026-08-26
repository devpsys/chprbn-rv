package ng.com.chprbn.mobile.core.sync

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure-Kotlin core of the sync engine, factored out of [SyncWorker] so the
 * dispatch logic is unit-testable without WorkManager.
 *
 * Pulls up to [batchSize] pending/failed rows, **groups them by
 * [SyncEntityType]**, and dispatches each type's full list of entityKeys
 * to the matching [SyncEntityHandler] in a single call. The handler is
 * expected to send one batched HTTP request and return one
 * [SyncOutcome] per input key; the runner then walks the queue rows in
 * FIFO order and persists each outcome.
 *
 * Per-row failures do not abort the batch — partial success is the
 * normal case. Cross-type ordering of the aggregated `errors` list
 * follows "all errors from type A in FIFO order, then all errors from
 * type B in FIFO order, …" (type order = order in which each type's
 * first job was enqueued).
 *
 * [clock] is injectable so tests can pin time. In production it's bound
 * to [Clock.System].
 */
@Singleton
class SyncBatchRunner @Inject constructor(
    private val syncJobDao: SyncJobDao,
    private val handlers: Map<SyncEntityType, @JvmSuppressWildcards SyncEntityHandler>,
    private val clock: Clock,
) {

    /**
     * @param resetAbandoned when `true`, every queue row currently
     * flipped to [SyncStatus.Abandoned] is re-enabled for retry
     * (status → Failed, attemptCount → 0) BEFORE the batch is picked.
     * Only user-initiated flows (Statistics "Sync Now") should pass
     * `true`; the background WorkManager keeps the current
     * cap-and-forget behaviour so a poison row can't burn bandwidth.
     * Fixes the "Sync Complete — nothing to sync" bug that stranded
     * abandoned rows on the Cached Records Failed tab with no user
     * recourse to retry them.
     */
    suspend fun runBatch(
        batchSize: Int = DEFAULT_BATCH_SIZE,
        resetAbandoned: Boolean = false,
    ): SyncBatchResult {
        if (resetAbandoned) syncJobDao.resetAbandonedForRetry()
        val jobs = syncJobDao.pendingAndFailed(limit = batchSize)
        if (jobs.isEmpty()) return SyncBatchResult.Empty

        var succeeded = 0
        var failed = 0
        val errors = mutableListOf<String>()

        // groupBy preserves insertion order, so each type's bucket is in
        // FIFO order and the type buckets themselves are ordered by the
        // enqueuedAt of each type's first job.
        val jobsByType: Map<SyncEntityType?, List<SyncJobEntity>> = jobs.groupBy { job ->
            runCatching { SyncEntityType.valueOf(job.entityType) }.getOrNull()
        }

        for ((type, typeJobs) in jobsByType) {
            val handler = type?.let { handlers[it] }
            val outcomes: Map<String, SyncOutcome> = when {
                type == null -> typeJobs.associate {
                    it.entityKey to SyncOutcome.Failure("Unknown entity type: ${it.entityType}")
                }
                handler == null -> typeJobs.associate {
                    it.entityKey to SyncOutcome.Failure("No handler bound for $type")
                }
                else -> runCatching { handler.uploadBatch(typeJobs.map { it.entityKey }) }
                    .getOrElse { t ->
                        val message = t.message ?: "Unknown error"
                        typeJobs.associate { it.entityKey to SyncOutcome.Failure(message) }
                    }
            }

            val attemptedAt = clock.nowMillis()
            for (job in typeJobs) {
                val outcome = outcomes[job.entityKey]
                    ?: SyncOutcome.Failure("Handler returned no result for ${job.entityKey}")
                when (outcome) {
                    is SyncOutcome.Success -> {
                        syncJobDao.delete(job.id)
                        succeeded++
                    }
                    is SyncOutcome.Drop -> {
                        // Handler signalled self-clean: no local row to upload.
                        // Delete without touching attemptCount so metrics stay
                        // truthful, but count as succeeded from the queue's
                        // perspective (the job is done).
                        syncJobDao.delete(job.id)
                        succeeded++
                    }
                    is SyncOutcome.Failure -> {
                        // Cap retries so a poison row (server permanently
                        // rejects, malformed clientId, …) doesn't sit in the
                        // queue forever. `markAttempted` increments the count
                        // then and there, so the next-attempt view is what we
                        // compare against MAX_ATTEMPTS.
                        val nextAttemptCount = job.attemptCount + 1
                        val nextStatus = if (nextAttemptCount >= MAX_ATTEMPTS) {
                            SyncStatus.Abandoned.name
                        } else {
                            SyncStatus.Failed.name
                        }
                        syncJobDao.markAttempted(
                            id = job.id,
                            status = nextStatus,
                            attemptedAt = attemptedAt,
                            error = outcome.message,
                        )
                        failed++
                        errors.add(outcome.message)
                    }
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

        /**
         * A failing row is retried up to this many times before it is
         * flipped to [SyncStatus.Abandoned] and left in the queue for
         * auditability. Chosen so a genuinely-transient issue (rolling
         * network drop, 15-minute backend outage) gets multiple shots,
         * but a poison row (server permanently rejects, malformed
         * clientId) stops burning bandwidth after ~5 attempts.
         */
        const val MAX_ATTEMPTS = 5
    }
}
