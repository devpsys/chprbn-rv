package ng.com.chprbn.mobile.feature.assessment.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One per-candidate project score (vs `PracticalScore` which is per-question).
 * Composite identity is `(scheduleId, candidateId)` — one project score per
 * candidate per schedule; saving a new value REPLACES the previous.
 *
 * `score` is `Double` because the UI accepts decimal entries (e.g. `"8.5"`).
 * The domain stores 0..maxScore with at most two decimal places; range +
 * precision validation lives in `RecordProjectScoreUseCase`.
 */
data class ProjectScore(
    val scheduleId: String,
    val candidateId: String,
    val score: Double,
    val maxScore: Int,
    val scoredAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null,
)
