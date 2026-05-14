package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * One row per `(paperId, candidateId)`. Composite PK enforces "one
 * attendance state per candidate per paper" — re-marking REPLACES.
 *
 * Indexed on `syncStatus` because the sync engine queries it to find
 * pending/failed rows, and on `(paperId)` for the per-paper aggregations
 * driving the statistics + paper-detail counters.
 */
@Entity(
    tableName = "attendance",
    primaryKeys = ["paperId", "candidateId"],
    indices = [
        Index("syncStatus"),
        Index("paperId"),
    ],
)
data class AttendanceEntity(
    val paperId: String,
    val candidateId: String,
    val status: String,
    val markedAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
    val lastSyncAttemptAt: Long? = null,
)
