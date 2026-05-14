package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface AssessmentPaperDao {

    @Query("SELECT * FROM assessment_papers WHERE scheduleId = :scheduleId")
    suspend fun getByScheduleId(scheduleId: String): AssessmentPaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(paper: AssessmentPaperEntity): Long

    @Query("DELETE FROM assessment_papers WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: String): Int

    @Query("DELETE FROM assessment_papers")
    suspend fun clearAll(): Int
}
