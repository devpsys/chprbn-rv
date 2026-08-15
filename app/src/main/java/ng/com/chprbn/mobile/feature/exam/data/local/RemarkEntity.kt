package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One remark per candidate — `candidateId` is the primary key, so
 * [RemarkDao.upsert]'s `REPLACE` conflict strategy overwrites whatever
 * remark (if any) was already on file for that candidate. `id` stays a
 * client-generated UUID, regenerated on every replace, purely as the
 * sync-queue correlation key (see `RemarkKey`/`RemarkSyncHandler`) — a
 * sync job enqueued against a since-replaced `id` finds no matching row
 * and self-cleans as a ghost job, same pattern as attendance.
 */
@Entity(
    tableName = "remarks",
    indices = [Index("syncStatus")],
)
data class RemarkEntity(
    @PrimaryKey val candidateId: String,
    val id: String,
    val paperId: String? = null,
    /** [ng.com.chprbn.mobile.feature.exam.presentation.RemarkType.code] — e.g. `"AE"`. Feeds `attendance/push-record`'s `remark` field. */
    @ColumnInfo(defaultValue = "")
    val code: String = "",
    val body: String,
    val severity: String,
    val createdAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
    val lastSyncAttemptAt: Long? = null,
)
