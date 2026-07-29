package ng.com.chprbn.mobile.core.sync

/**
 * Cross-feature helper for `Api*SyncRemoteSource` implementations that walk
 * a batch response's per-row `results[]` array. Folds a transport-level
 * [Result] plus the per-row shape into one `clientId → Result<Unit>` map
 * that the sync handler layer expects.
 *
 * Contract:
 *
 * - **Transport success + per-row accepted** → `Result.success(Unit)`.
 * - **Transport success + per-row rejected** → `Result.failure` with
 *   [errorOf] (falls back to a generic message).
 * - **Transport success + no matching row for a clientId** →
 *   `Result.failure(IllegalStateException("Server returned no result for X"))`.
 * - **Transport failure** → the same throwable applied to every input
 *   clientId so the handler can retry uniformly.
 *
 * Extracted from the exam and assessment-side copies (A-S4 audit fix) to
 * keep envelope semantics identical across both features.
 */
internal fun <R> foldBatchResults(
    clientIds: List<String>,
    transportOutcome: Result<List<R>>,
    acceptedOf: (R) -> Boolean,
    errorOf: (R) -> String?,
    clientIdOf: (R) -> String?,
): Map<String, Result<Unit>> = transportOutcome.fold(
    onSuccess = { rows ->
        val byClientId: Map<String, R> = rows.associateBy { clientIdOf(it).orEmpty() }
        clientIds.associateWith { id ->
            val r = byClientId[id]
            when {
                r == null -> Result.failure(
                    IllegalStateException("Server returned no result for $id"),
                )
                acceptedOf(r) -> Result.success(Unit)
                else -> Result.failure(
                    IllegalStateException(errorOf(r) ?: "Server rejected row."),
                )
            }
        }
    },
    onFailure = { t ->
        clientIds.associateWith { Result.failure<Unit>(t) }
    },
)
