package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Owns reads/writes to both `assessment_candidates` (the canonical
 * candidate roster) and `schedule_candidate_assignments` (the many-to-many
 * join). Kept in one DAO because every assignment-write happens alongside
 * a candidate-upsert and the two tables are queried together far more
 * often than separately.
 *
 * Projection [AssessmentCandidateRowProjection] backs the directory query;
 * the aggregate score + sync derivation runs in SQL so the JVM never sees
 * the per-question score rows.
 */
@Dao
@JvmSuppressWildcards
interface AssessmentCandidateDao {

    @Query("SELECT * FROM assessment_candidates WHERE id = :candidateId")
    suspend fun getById(candidateId: String): AssessmentCandidateEntity?

    @Query(
        """
        SELECT c.* FROM assessment_candidates c
        INNER JOIN schedule_candidate_assignments a ON a.candidateId = c.id
        WHERE a.scheduleId = :scheduleId AND a.candidateId = :candidateId
        """,
    )
    suspend fun getForSchedule(
        scheduleId: String,
        candidateId: String,
    ): AssessmentCandidateEntity?

    /**
     * Aggregates per-candidate scoring state for the candidates directory.
     *
     * - `aggregateScore`: SUM of practical scores + the project score
     *   (rounded to Int) — `0` when nothing is scored.
     * - `scoredQuestions` / `totalQuestions`: counts driving the
     *   "Synced/Unsynced" pill.
     * - `syncStatus`: derived in SQL with explicit priority
     *   `Failed > Pending > Synced` — if any row is `Failed` the
     *   candidate is `Failed`; else if any is `Pending`, the candidate
     *   is `Pending`; else `Synced`. Falls back to `Synced` for
     *   candidates with no scored rows (the screen reads "no scoring yet"
     *   from `scoredQuestions == 0`, so the pill colour is moot there).
     *
     * `:query` is a LIKE pattern — caller supplies `%foo%` or `""` for
     * no-filter. Empty match returns the full cohort.
     */
    @Query(
        """
        SELECT
            c.id                                  AS candidateId,
            c.examNumber                          AS examNumber,
            c.fullName                            AS fullName,
            c.photoUrl                            AS photoUrl,
            COALESCE((
                SELECT SUM(ps.score) FROM practical_scores ps
                WHERE ps.scheduleId = :scheduleId AND ps.candidateId = c.id
            ), 0)
            + COALESCE((
                SELECT ROUND(pjs.score) FROM project_scores pjs
                WHERE pjs.scheduleId = :scheduleId AND pjs.candidateId = c.id
            ), 0)                                 AS aggregateScore,
            COALESCE((
                SELECT COUNT(*) FROM practical_scores ps
                WHERE ps.scheduleId = :scheduleId AND ps.candidateId = c.id
            ), 0)                                 AS scoredQuestions,
            COALESCE((
                SELECT COUNT(*) FROM section_questions sq
                INNER JOIN practical_sections sec ON sec.id = sq.sectionId
                WHERE sec.scheduleId = :scheduleId
            ), 0)                                 AS totalQuestions,
            CASE
                WHEN EXISTS(
                    SELECT 1 FROM practical_scores ps
                    WHERE ps.scheduleId = :scheduleId AND ps.candidateId = c.id AND ps.syncStatus = 'Failed'
                ) OR EXISTS(
                    SELECT 1 FROM project_scores pjs
                    WHERE pjs.scheduleId = :scheduleId AND pjs.candidateId = c.id AND pjs.syncStatus = 'Failed'
                ) THEN 'Failed'
                WHEN EXISTS(
                    SELECT 1 FROM practical_scores ps
                    WHERE ps.scheduleId = :scheduleId AND ps.candidateId = c.id AND ps.syncStatus = 'Pending'
                ) OR EXISTS(
                    SELECT 1 FROM project_scores pjs
                    WHERE pjs.scheduleId = :scheduleId AND pjs.candidateId = c.id AND pjs.syncStatus = 'Pending'
                ) THEN 'Pending'
                ELSE 'Synced'
            END                                   AS syncStatus
        FROM assessment_candidates c
        INNER JOIN schedule_candidate_assignments a ON a.candidateId = c.id
        WHERE a.scheduleId = :scheduleId
          AND (:query = '' OR c.fullName LIKE :query OR c.examNumber LIKE :query)
        ORDER BY c.fullName ASC
        """,
    )
    suspend fun rowsForSchedule(
        scheduleId: String,
        query: String,
    ): List<AssessmentCandidateRowProjection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(candidates: List<AssessmentCandidateEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignments(assignments: List<ScheduleCandidateAssignmentEntity>): List<Long>

    @Query("DELETE FROM schedule_candidate_assignments WHERE scheduleId = :scheduleId")
    suspend fun deleteAssignmentsForSchedule(scheduleId: String): Int

    @Query("DELETE FROM assessment_candidates")
    suspend fun clearCandidates(): Int
}
