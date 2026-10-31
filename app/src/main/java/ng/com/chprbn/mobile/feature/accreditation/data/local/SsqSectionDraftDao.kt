package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface SsqSectionDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SsqSectionDraftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<SsqSectionDraftEntity>): List<Long>

    @Query(
        """
        SELECT * FROM ssq_section_drafts
        WHERE assessmentId = :assessmentId AND sectionKey = :sectionKey
        LIMIT 1
        """,
    )
    suspend fun get(assessmentId: String, sectionKey: String): SsqSectionDraftEntity?

    @Query("SELECT * FROM ssq_section_drafts WHERE assessmentId = :assessmentId ORDER BY sectionKey ASC")
    suspend fun getForAssessment(assessmentId: String): List<SsqSectionDraftEntity>

    @Query("DELETE FROM ssq_section_drafts WHERE assessmentId IN (:assessmentIds)")
    suspend fun deleteForAssessments(assessmentIds: List<String>): Int

    @Query(
        """
        UPDATE ssq_section_drafts
        SET status = :status, itemsCompleted = totalItems
        WHERE assessmentId = :assessmentId
        """,
    )
    suspend fun markAllSaved(assessmentId: String, status: String): Int
}
