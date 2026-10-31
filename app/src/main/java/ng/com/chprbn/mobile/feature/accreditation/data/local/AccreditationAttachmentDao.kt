package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface AccreditationAttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AccreditationAttachmentEntity): Long

    @Query("SELECT * FROM accreditation_attachments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AccreditationAttachmentEntity?

    @Query(
        """
        SELECT * FROM accreditation_attachments
        WHERE assessmentId = :assessmentId AND parentKey = :parentKey
        ORDER BY title ASC
        """,
    )
    suspend fun listForParent(assessmentId: String, parentKey: String): List<AccreditationAttachmentEntity>

    @Query("SELECT COUNT(*) FROM accreditation_attachments WHERE assessmentId = :assessmentId")
    suspend fun countForAssessment(assessmentId: String): Int

    @Query("DELETE FROM accreditation_attachments WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM accreditation_attachments WHERE assessmentId IN (:assessmentIds)")
    suspend fun deleteForAssessments(assessmentIds: List<String>): Int
}
