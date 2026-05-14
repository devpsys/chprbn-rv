package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * One per-candidate project score. Composite PK `(scheduleId, candidateId)`
 * enforces one row per candidate per schedule; re-scoring REPLACES.
 * `score` is REAL (`Double`) to match the UI's decimal entry; precision is
 * enforced upstream by `RecordProjectScoreUseCase`.
 */
@Entity(
    tableName = "project_scores",
    primaryKeys = ["scheduleId", "candidateId"],
    indices = [Index("syncStatus")],
)
data class ProjectScoreEntity(
    val scheduleId: String,
    val candidateId: String,
    val score: Double,
    val maxScore: Int,
    val scoredAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
)
