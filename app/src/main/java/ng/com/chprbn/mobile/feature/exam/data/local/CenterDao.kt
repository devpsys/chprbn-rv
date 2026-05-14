package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface CenterDao {

    @Query("SELECT * FROM centers WHERE id = :centerId")
    suspend fun getById(centerId: String): CenterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(center: CenterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(centers: List<CenterEntity>): List<Long>

    @Query("DELETE FROM centers")
    suspend fun clearAll(): Int
}
