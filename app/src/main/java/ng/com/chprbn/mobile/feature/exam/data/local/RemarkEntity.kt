package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only — `id` is a client-generated UUID until the server assigns
 * a real one (which then REPLACES via the upsert). Indexed on
 * `candidateId` (the row-list query path) and `syncStatus` (the sync
 * worker's pending lookup).
 */
@Entity(
    tableName = "remarks",
    indices = [Index("candidateId"), Index("syncStatus")],
)
data class RemarkEntity(
    @PrimaryKey val id: String,
    val candidateId: String,
    val paperId: String? = null,
    val body: String,
    val severity: String,
    val createdAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
    val lastSyncAttemptAt: Long? = null,
)
