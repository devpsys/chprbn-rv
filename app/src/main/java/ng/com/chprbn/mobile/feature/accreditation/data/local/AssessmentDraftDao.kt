package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface AssessmentDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AssessmentDraftEntity): Long

    @Query("SELECT * FROM assessment_drafts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AssessmentDraftEntity?

    @Query("SELECT * FROM assessment_drafts WHERE institutionId = :institutionId LIMIT 1")
    suspend fun getByInstitutionId(institutionId: String): AssessmentDraftEntity?

    @Query("SELECT * FROM assessment_drafts ORDER BY updatedAt DESC")
    suspend fun getAll(): List<AssessmentDraftEntity>

    @Query("SELECT * FROM assessment_drafts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AssessmentDraftEntity>>

    @Query("DELETE FROM assessment_drafts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>): Int

    @Query("UPDATE assessment_drafts SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long): Int
}
