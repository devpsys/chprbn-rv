package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface AttendanceDao {

    @Query(
        """
        SELECT * FROM attendance
        WHERE paperId = :paperId AND candidateId = :candidateId
        """,
    )
    suspend fun getOne(paperId: String, candidateId: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity): Long

    @Query(
        """
        UPDATE attendance
        SET syncStatus = :syncStatus,
            syncError = :syncError,
            lastSyncAttemptAt = :lastSyncAttemptAt
        WHERE paperId = :paperId AND candidateId = :candidateId
        """,
    )
    suspend fun updateSyncMetadata(
        paperId: String,
        candidateId: String,
        syncStatus: String,
        syncError: String?,
        lastSyncAttemptAt: Long?,
    ): Int

    @Query("SELECT * FROM attendance WHERE syncStatus IN ('Pending', 'Failed') LIMIT :limit")
    suspend fun pendingAndFailed(limit: Int = 50): List<AttendanceEntity>

    @Query("SELECT COUNT(*) FROM attendance WHERE paperId = :paperId AND status = :status")
    suspend fun countByStatusForPaper(paperId: String, status: String): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE syncStatus = :syncStatus")
    suspend fun countBySyncStatus(syncStatus: String): Int

    @Query("SELECT COUNT(*) FROM attendance")
    suspend fun totalCount(): Int

    @Query("SELECT MAX(markedAt) FROM attendance")
    suspend fun mostRecentMarkedAt(): Long?

    @Query("DELETE FROM attendance")
    suspend fun clearAll(): Int
}
