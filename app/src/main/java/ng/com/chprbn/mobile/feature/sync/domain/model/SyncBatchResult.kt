package ng.com.chprbn.mobile.feature.sync.domain.model

/**
 * Outcome of a batch sync run. Each record is attempted independently so partial success is expected.
 */
data class SyncBatchResult(
    val attempted: Int,
    val succeeded: Int,
    val failed: Int,
    val errors: List<String> = emptyList()
)
