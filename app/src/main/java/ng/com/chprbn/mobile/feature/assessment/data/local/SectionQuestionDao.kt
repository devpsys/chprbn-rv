package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface SectionQuestionDao {

    @Query("SELECT * FROM section_questions WHERE sectionId = :sectionId ORDER BY number ASC")
    suspend fun getBySectionId(sectionId: String): List<SectionQuestionEntity>

    @Query(
        """
        SELECT q.* FROM section_questions q
        INNER JOIN practical_sections s ON s.id = q.sectionId
        WHERE s.scheduleId = :scheduleId
        """,
    )
    suspend fun getByScheduleId(scheduleId: String): List<SectionQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(questions: List<SectionQuestionEntity>): List<Long>

    @Query(
        """
        DELETE FROM section_questions
        WHERE sectionId IN (SELECT id FROM practical_sections WHERE scheduleId = :scheduleId)
        """,
    )
    suspend fun deleteByScheduleId(scheduleId: String): Int

    @Query("DELETE FROM section_questions")
    suspend fun clearAll(): Int
}
