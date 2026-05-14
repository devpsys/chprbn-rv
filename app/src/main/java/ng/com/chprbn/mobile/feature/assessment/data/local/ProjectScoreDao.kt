package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface ProjectScoreDao {

    @Query("SELECT * FROM project_scores WHERE scheduleId = :scheduleId AND candidateId = :candidateId")
    suspend fun getOne(scheduleId: String, candidateId: String): ProjectScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(score: ProjectScoreEntity): Long

    @Query(
        """
        UPDATE project_scores
        SET syncStatus = :syncStatus, syncError = :syncError
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId
        """,
    )
    suspend fun updateSyncMetadata(
        scheduleId: String,
        candidateId: String,
        syncStatus: String,
        syncError: String?,
    ): Int

    @Query("SELECT * FROM project_scores WHERE syncStatus IN ('Pending', 'Failed') LIMIT :limit")
    suspend fun pendingAndFailed(limit: Int = 50): List<ProjectScoreEntity>

    @Query(
        """
        SELECT COUNT(*) FROM project_scores
        WHERE scheduleId = :scheduleId AND syncStatus = :syncStatus
        """,
    )
    suspend fun countByStatusForSchedule(scheduleId: String, syncStatus: String): Int

    @Query("DELETE FROM project_scores WHERE scheduleId = :scheduleId")
    suspend fun deleteForSchedule(scheduleId: String): Int
}
