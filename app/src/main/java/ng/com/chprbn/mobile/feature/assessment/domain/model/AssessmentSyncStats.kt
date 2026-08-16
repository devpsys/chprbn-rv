package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Paper-detail sync chrome: how many practical + project rows are still
 * Pending/Failed, and the most recent successful sync timestamp (max
 * `scoredAt` among Synced rows). `lastSyncAt` is null when nothing has
 * synced yet.
 */
data class AssessmentSyncStats(
    val pendingSyncCount: Int,
    val lastSyncAt: Long?,
)
