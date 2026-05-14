package ng.com.chprbn.mobile.core.domain.model

/**
 * Result of running one pass of the sync queue. Sync is partial-success by
 * design (a batch never fails as a whole — individual rows can succeed or fail
 * independently), so the result is a counter rather than a sealed hierarchy.
 *
 * `errors` carries one human-readable message per failed row, in order, for
 * surfacing in a snackbar or sync-history screen.
 */
data class SyncBatchResult(
    val attempted: Int,
    val succeeded: Int,
    val failed: Int,
    val errors: List<String> = emptyList(),
) {
    companion object {
        val Empty = SyncBatchResult(attempted = 0, succeeded = 0, failed = 0)
    }
}
