package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface RemarkDao {

    @Query("SELECT * FROM remarks WHERE id = :id")
    suspend fun getById(id: String): RemarkEntity?

    @Query("SELECT * FROM remarks WHERE candidateId = :candidateId ORDER BY createdAt DESC")
    suspend fun getForCandidate(candidateId: String): List<RemarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(remark: RemarkEntity): Long

    @Query(
        """
        UPDATE remarks
        SET syncStatus = :syncStatus,
            syncError = :syncError,
            lastSyncAttemptAt = :lastSyncAttemptAt
        WHERE id = :id
        """,
    )
    suspend fun updateSyncMetadata(
        id: String,
        syncStatus: String,
        syncError: String?,
        lastSyncAttemptAt: Long?,
    ): Int

    @Query("SELECT * FROM remarks WHERE syncStatus IN ('Pending', 'Failed') LIMIT :limit")
    suspend fun pendingAndFailed(limit: Int = 50): List<RemarkEntity>

    @Query("DELETE FROM remarks")
    suspend fun clearAll(): Int
}
