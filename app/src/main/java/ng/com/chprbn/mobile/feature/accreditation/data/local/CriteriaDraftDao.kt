package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface CriteriaDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CriteriaDraftEntity): Long

    @Query("SELECT * FROM criteria_drafts WHERE assessmentId = :assessmentId LIMIT 1")
    suspend fun get(assessmentId: String): CriteriaDraftEntity?

    @Query("DELETE FROM criteria_drafts WHERE assessmentId IN (:assessmentIds)")
    suspend fun deleteForAssessments(assessmentIds: List<String>): Int
}
