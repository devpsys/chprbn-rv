package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Owns reads/writes to both `candidates` and `paper_candidate_assignments`.
 * Kept in one DAO because every assignment-write happens alongside a
 * candidate-upsert.
 *
 * [rowsForPaper] is the heavyweight join backing `ExamCandidatesScreen`:
 * candidates × attendance (LEFT JOIN, null when not yet marked) × remark
 * counts. Filters apply at SQL: an [attendanceFilter] of `"All"`
 * short-circuits the status filter; the [query] is a LIKE pattern (`""`
 * disables it).
 */
@Dao
@JvmSuppressWildcards
interface CandidateDao {

    @Query("SELECT * FROM candidates WHERE id = :candidateId")
    suspend fun getById(candidateId: String): CandidateEntity?

    /**
     * Batch label-lookup used by `CachedRecordsRepositoryImpl` — the
     * assessment-side score rows live in a different DB, so the repo
     * pulls their candidate labels through here in one query rather
     * than N `getById` hops.
     */
    @Query("SELECT * FROM candidates WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<CandidateEntity>

    @Query("SELECT * FROM candidates WHERE examNumber = :examNumber LIMIT 1")
    suspend fun getByExamNumber(examNumber: String): CandidateEntity?

    @Query(
        """
        SELECT
            c.id                AS candidateId,
            c.examNumber        AS examNumber,
            c.fullName          AS fullName,
            c.photoUrl          AS photoUrl,
            a.status            AS attendanceStatus,
            a.markedAt          AS attendanceMarkedAt,
            a.syncStatus        AS attendanceSyncStatus,
            a.syncError         AS attendanceSyncError,
            COALESCE((
                SELECT COUNT(*) FROM remarks
                WHERE candidateId = c.id
            ), 0)               AS remarkCount
        FROM candidates c
        INNER JOIN paper_candidate_assignments pca ON pca.candidateId = c.id
        LEFT JOIN attendance a ON a.candidateId = c.id AND a.paperId = pca.paperId
        WHERE pca.paperId = :paperId
          AND (:attendanceFilter = 'All' OR a.status = :attendanceFilter)
          AND (:query = '' OR c.fullName LIKE :query OR c.examNumber LIKE :query)
        ORDER BY c.fullName ASC
        """,
    )
    suspend fun rowsForPaper(
        paperId: String,
        attendanceFilter: String,
        query: String,
    ): List<ExamCandidateRowProjection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(candidates: List<CandidateEntity>): List<Long>

    /**
     * Additive-merge insert used by `downloadDossier` — pre-existing
     * candidate rows (same PK) are left untouched, so a re-download that
     * ships the same candidate with, say, a fresher photoUrl does NOT
     * overwrite the local row. The return list carries the new rowId
     * for newly-inserted rows and `-1L` for the skipped/conflicting
     * ones; callers count the non-negatives to report "N new candidates."
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(candidates: List<CandidateEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignments(assignments: List<PaperCandidateAssignmentEntity>): List<Long>

    /** Backs [ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceSyncHandler]'s scheduledCandidateId/scheduleId lookup at sync time. */
    @Query("SELECT * FROM paper_candidate_assignments WHERE paperId = :paperId AND candidateId = :candidateId")
    suspend fun getAssignment(paperId: String, candidateId: String): PaperCandidateAssignmentEntity?

    @Query("SELECT COUNT(*) FROM paper_candidate_assignments")
    suspend fun assignmentCount(): Int

    /**
     * Live count of candidates assigned to [paperId]. Consumed by the
     * assessment feature as the fallback denominator for a PE/PA
     * paper's progress pill before the schedule's package has been
     * downloaded (see `AssessmentCandidateRepositoryImpl.observeAssignedCount`).
     */
    @Query("SELECT COUNT(*) FROM paper_candidate_assignments WHERE paperId = :paperId")
    fun observeCountForPaper(paperId: String): Flow<Int>

    /**
     * Plain candidate list assigned to [paperId] — no attendance/remark
     * join, no LIKE filter. Used by the assessment feature to render a
     * shell candidate list for a PE/PA paper before its package is
     * downloaded.
     */
    @Query(
        """
        SELECT c.* FROM candidates c
        INNER JOIN paper_candidate_assignments pca ON pca.candidateId = c.id
        WHERE pca.paperId = :paperId
        ORDER BY c.fullName ASC
        """,
    )
    suspend fun candidatesForPaper(paperId: String): List<CandidateEntity>

    /**
     * Scoped exam-number lookup — the assessment feature falls back to
     * this when the schedule's practical package hasn't been downloaded
     * yet but the candidate is on the paper's dossier roster.
     */
    @Query(
        """
        SELECT c.* FROM candidates c
        INNER JOIN paper_candidate_assignments pca ON pca.candidateId = c.id
        WHERE pca.paperId = :paperId AND c.examNumber = :examNumber
        LIMIT 1
        """,
    )
    suspend fun candidateForPaperByExamNumber(
        paperId: String,
        examNumber: String,
    ): CandidateEntity?

    @Query("DELETE FROM paper_candidate_assignments")
    suspend fun clearAssignments(): Int

    @Query("DELETE FROM candidates")
    suspend fun clearCandidates(): Int
}
