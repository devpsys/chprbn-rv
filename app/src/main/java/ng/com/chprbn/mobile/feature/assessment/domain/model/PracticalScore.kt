package ng.com.chprbn.mobile.feature.assessment.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One per-question score captured by the examiner. Composite identity is
 * `(scheduleId, candidateId, questionId)` — re-scoring the same question for
 * the same candidate REPLACES the previous value (no append-only history).
 *
 * `score == 0` is a valid scored value (not a "not scored" sentinel); the
 * scoring screen carries an explicit flag for "has the examiner touched this
 * row yet" by looking at the presence of the row, not the score value.
 *
 * This is one of the two highest-volume write rows in the app — every stepper
 * tap upserts a row and enqueues a `SyncJobEntity`.
 */
data class PracticalScore(
    val scheduleId: String,
    val candidateId: String,
    val questionId: String,
    val score: Int,
    val scoredAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null,
)
