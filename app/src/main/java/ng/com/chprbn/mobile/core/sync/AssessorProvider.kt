package ng.com.chprbn.mobile.core.sync

import ng.com.chprbn.mobile.core.sync.dto.AssessorDto

/**
 * Builds the [AssessorDto] attached to every push-record request.
 * Returns `null` when the cache has no user or the user's row is missing
 * a load-bearing field (numeric assessor id / username) — the sync
 * remote sources use `null` as a signal to fail the batch fast with a
 * clear "Sign in again to sync" message rather than send a request the
 * server will 4xx anyway.
 */
interface AssessorProvider {
    suspend fun current(): AssessorDto?
}
