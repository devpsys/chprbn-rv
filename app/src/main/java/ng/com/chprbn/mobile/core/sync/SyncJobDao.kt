package ng.com.chprbn.mobile.core.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface SyncJobDao {

    /**
     * Inserts a new job or, when one already exists for the same
     * (entityType, entityKey) pair, replaces it — re-queueing the row from
     * scratch with `attemptCount = 0`. Feature repositories call this after
     * every local write.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(job: SyncJobEntity): Long

    @Query(
        """
        SELECT * FROM sync_jobs
        WHERE status IN ('Pending', 'Failed')
        ORDER BY enqueuedAt ASC
        LIMIT :limit
        """,
    )
    suspend fun pendingAndFailed(limit: Int = 50): List<SyncJobEntity>

    @Query("SELECT COUNT(*) FROM sync_jobs WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM sync_jobs WHERE entityType = :entityType AND status = :status")
    suspend fun countByTypeAndStatus(entityType: String, status: String): Int

    @Query("SELECT * FROM sync_jobs ORDER BY enqueuedAt DESC")
    fun observeAll(): Flow<List<SyncJobEntity>>

    @Query(
        """
        UPDATE sync_jobs
        SET status = :status,
            attemptCount = attemptCount + 1,
            lastAttemptAt = :attemptedAt,
            lastError = :error
        WHERE id = :id
        """,
    )
    suspend fun markAttempted(id: Long, status: String, attemptedAt: Long, error: String?): Int

    @Query("DELETE FROM sync_jobs WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM sync_jobs WHERE status = 'Synced'")
    suspend fun pruneSynced(): Int

    @Query("DELETE FROM sync_jobs")
    suspend fun clearAll(): Int
}
