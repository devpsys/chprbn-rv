package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignments(assignments: List<PaperCandidateAssignmentEntity>): List<Long>

    @Query("DELETE FROM paper_candidate_assignments")
    suspend fun clearAssignments(): Int

    @Query("DELETE FROM candidates")
    suspend fun clearCandidates(): Int
}
