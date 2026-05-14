package ng.com.chprbn.mobile.core.sync

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per pending upload. The [entityKey] is a feature-defined composite
 * encoded as a string (e.g. `"paperId/candidateId"` for attendance) — opaque
 * to the queue, interpreted only by the matching [SyncEntityHandler].
 *
 * Rows are inserted by feature repository impls after a successful local
 * write, and removed (or flipped to `Synced`) by the [SyncWorker] once the
 * remote upload accepts them. Failed rows stay in the table with
 * [attemptCount] incremented and [lastError] populated; the worker re-attempts
 * them with exponential backoff.
 */
@Entity(
    tableName = "sync_jobs",
    indices = [
        Index("entityType"),
        Index("status"),
        Index(value = ["entityType", "entityKey"], unique = true),
    ],
)
data class SyncJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,                // SyncEntityType.name
    val entityKey: String,                 // composite key encoded as String
    val enqueuedAt: Long,
    val status: String,                    // SyncStatus.name
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
)
