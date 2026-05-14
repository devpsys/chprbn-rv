package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface AssessmentScheduleDao {

    @Query("SELECT * FROM assessment_schedules ORDER BY date ASC")
    suspend fun getAll(): List<AssessmentScheduleEntity>

    @Query("SELECT * FROM assessment_schedules WHERE id = :scheduleId")
    suspend fun getById(scheduleId: String): AssessmentScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: AssessmentScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(schedules: List<AssessmentScheduleEntity>): List<Long>

    @Query("UPDATE assessment_schedules SET syncStatus = :syncStatus WHERE id = :scheduleId")
    suspend fun updateSyncStatus(scheduleId: String, syncStatus: String): Int

    @Query("DELETE FROM assessment_schedules")
    suspend fun clearAll(): Int
}
