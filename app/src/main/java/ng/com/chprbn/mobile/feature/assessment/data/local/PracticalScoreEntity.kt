package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * One per-question score. Composite PK
 * `(scheduleId, candidateId, questionId)` enforces the "one score per
 * candidate per question" invariant; re-scoring REPLACES.
 *
 * Indexed on `syncStatus` because the sync engine queries it to find
 * pending/failed rows, and on `(scheduleId, candidateId)` for the
 * per-candidate hub aggregation.
 */
@Entity(
    tableName = "practical_scores",
    primaryKeys = ["scheduleId", "candidateId", "questionId"],
    indices = [
        Index("syncStatus"),
        Index(value = ["scheduleId", "candidateId"]),
    ],
)
data class PracticalScoreEntity(
    val scheduleId: String,
    val candidateId: String,
    val questionId: String,
    val score: Int,
    val scoredAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
)
