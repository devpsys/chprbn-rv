package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    /**
     * Flow variant of [getForCandidate] — emits a fresh snapshot every
     * time a score for this candidate is upserted. Backs the practical-
     * sections screen so navigating back from a scoring child screen
     * shows the updated section summary without a manual refresh (A-S6
     * audit fix).
     */
    @Query(
        """
        SELECT * FROM practical_scores
        WHERE scheduleId = :scheduleId AND candidateId = :candidateId
        """,
    )
    fun observeForCandidate(
        scheduleId: String,
        candidateId: String,
    ): Flow<List<PracticalScoreEntity>>

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

    /**
     * Live pending/failed rows for the Cached Records screen. The
     * repo hydrates candidate + paper labels from `exam.db` since
     * Room can't cross-DB join. [statuses] lets the same query serve
     * the Pending tab (`['Pending']`) and the Failed tab
     * (`['Failed', 'Abandoned']`).
     */
    @Query(
        """
        SELECT * FROM practical_scores
        WHERE syncStatus IN (:statuses)
        ORDER BY scoredAt DESC
        """,
    )
    fun observeByStatus(statuses: List<String>): Flow<List<PracticalScoreEntity>>

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

    @Query("SELECT COUNT(*) FROM practical_scores")
    suspend fun totalCount(): Int

    /**
     * Distinct candidates with at least one practical score, counted
     * per schedule. Two question rows for the same candidate on the
     * same paper still count as one assessed candidate.
     */
    @Query("SELECT COUNT(DISTINCT scheduleId || '|' || candidateId) FROM practical_scores")
    suspend fun assessedCandidateCount(): Int

    @Query("SELECT COUNT(*) FROM practical_scores WHERE syncStatus = :syncStatus")
    suspend fun countByStatus(syncStatus: String): Int

    @Query("SELECT MAX(scoredAt) FROM practical_scores")
    suspend fun mostRecentScoredAt(): Long?

    @Query(
        """
        SELECT MAX(scoredAt) FROM practical_scores
        WHERE scheduleId = :scheduleId AND syncStatus = 'Synced'
        """,
    )
    suspend fun mostRecentSyncedAt(scheduleId: String): Long?

    @Query("DELETE FROM practical_scores WHERE scheduleId = :scheduleId")
    suspend fun deleteForSchedule(scheduleId: String): Int

    /**
     * Live count of distinct candidates who have at least one score row for
     * the schedule. Drives the paper-detail "progress" pill (A-S5 audit fix
     * — was a hardcoded 100%). Re-emits on every score upsert.
     *
     * "Distinct scored candidates" is a pragmatic approximation of
     * completion: the design's "checked-in" concept doesn't yet track
     * per-candidate submission state, so we use "started scoring" as the
     * signal until that lands.
     */
    @Query(
        """
        SELECT COUNT(DISTINCT candidateId) FROM practical_scores
        WHERE scheduleId = :scheduleId
        """,
    )
    fun observeStartedCandidateCountForSchedule(scheduleId: String): Flow<Int>

    /** Used by the SessionCleaner on logout — global wipe. */
    @Query("DELETE FROM practical_scores")
    suspend fun clearAll(): Int
}
