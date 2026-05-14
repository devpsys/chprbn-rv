package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface PracticalSectionDao {

    @Query("SELECT * FROM practical_sections WHERE scheduleId = :scheduleId ORDER BY ordering ASC")
    suspend fun getByScheduleId(scheduleId: String): List<PracticalSectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sections: List<PracticalSectionEntity>): List<Long>

    @Query("DELETE FROM practical_sections WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: String): Int

    @Query("DELETE FROM practical_sections")
    suspend fun clearAll(): Int
}
