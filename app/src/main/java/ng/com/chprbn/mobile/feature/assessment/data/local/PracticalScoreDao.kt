package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Per-question scoring read/write surface. The composite PK
 * `(scheduleId, candidateId, questionId)` makes upsert idempotent — a
 * second tap on the stepper REPLACES the previous score row, never
 * appends.
 *
 * Sync-engine queries:
 * - [pendingAndFailed] feeds the worker's per-row dispatch.
 * - [updateSyncMetadata] is the worker's write-back path.
 * - [countByStatusForSchedule] / [countByStatusForCandidate] drive the
 *   per-schedule / per-candidate `syncStatus` pills without pulling rows
 *   into the JVM.
 */
@Dao
@JvmSuppressWildcards
interface PracticalScoreDao {

    @Query(
        """
        SELECT * FROM practical_scores
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId AND questionId = :questionId
        """,
    )
    suspend fun getOne(
        scheduleId: String,
        candidateId: String,
        questionId: String,
    ): PracticalScoreEntity?

    @Query(
        """
        SELECT * FROM practical_scores
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId
        """,
    )
    suspend fun getForCandidate(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalScoreEntity>

    @Query(
        """
        SELECT ps.* FROM practical_scores ps
        INNER JOIN section_questions sq ON sq.id = ps.questionId
        WHERE ps.scheduleId = :scheduleId AND ps.candidateId = :candidateId AND sq.sectionId = :sectionId
        """,
    )
    suspend fun getForSection(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): List<PracticalScoreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(score: PracticalScoreEntity): Long

    @Query(
        """
        UPDATE practical_scores
        SET syncStatus = :syncStatus, syncError = :syncError
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId AND questionId = :questionId
        """,
    )
    suspend fun updateSyncMetadata(
        scheduleId: String,
        candidateId: String,
        questionId: String,
        syncStatus: String,
        syncError: String?,
    ): Int

    @Query("SELECT * FROM practical_scores WHERE syncStatus IN ('Pending', 'Failed') LIMIT :limit")
    suspend fun pendingAndFailed(limit: Int = 50): List<PracticalScoreEntity>

    @Query(
        """
        SELECT COUNT(*) FROM practical_scores
        WHERE scheduleId = :scheduleId AND syncStatus = :syncStatus
        """,
    )
    suspend fun countByStatusForSchedule(scheduleId: String, syncStatus: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM practical_scores
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId AND syncStatus = :syncStatus
        """,
    )
    suspend fun countByStatusForCandidate(
        scheduleId: String,
        candidateId: String,
        syncStatus: String,
    ): Int

    @Query("DELETE FROM practical_scores WHERE scheduleId = :scheduleId")
    suspend fun deleteForSchedule(scheduleId: String): Int
}
