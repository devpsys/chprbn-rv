package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface PaperDao {

    @Query("SELECT * FROM papers WHERE centerId = :centerId ORDER BY startAt ASC")
    suspend fun getForCenter(centerId: String): List<PaperEntity>

    @Query("SELECT * FROM papers ORDER BY startAt ASC")
    suspend fun getAll(): List<PaperEntity>

    /**
     * Papers scheduled today whose kind is NOT `Practical` / `Project` —
     * i.e. everything the exam-papers screen should show. PE (`Practical`)
     * and PA (`Project`) are surfaced by the assessment feature via
     * [getAssessmentPapers] (same underlying rows, different filter).
     */
    @Query(
        "SELECT * FROM papers WHERE paperKind NOT IN ('Practical', 'Project') ORDER BY startAt ASC",
    )
    suspend fun getExamPapers(): List<PaperEntity>

    /**
     * Papers scheduled today whose kind IS `Practical` / `Project` — the
     * assessment feature reads these and adapts them into
     * `AssessmentSchedule` rows. Replaces the deprecated dedicated
     * `/assessments/schedules` endpoint.
     */
    @Query(
        "SELECT * FROM papers WHERE paperKind IN ('Practical', 'Project') ORDER BY startAt ASC",
    )
    suspend fun getAssessmentPapers(): List<PaperEntity>

    @Query("SELECT * FROM papers WHERE id = :paperId")
    suspend fun getById(paperId: String): PaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(papers: List<PaperEntity>): List<Long>

    @Query("DELETE FROM papers")
    suspend fun clearAll(): Int
}
