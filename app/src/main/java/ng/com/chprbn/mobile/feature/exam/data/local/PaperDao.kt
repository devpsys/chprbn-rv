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

    @Query("SELECT * FROM papers WHERE id = :paperId")
    suspend fun getById(paperId: String): PaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(papers: List<PaperEntity>): List<Long>

    @Query("DELETE FROM papers")
    suspend fun clearAll(): Int
}
