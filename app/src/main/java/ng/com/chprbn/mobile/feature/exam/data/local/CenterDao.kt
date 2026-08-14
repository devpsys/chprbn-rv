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

    /**
     * A dossier download always clears and replaces the whole `centers`
     * table (see `ExamSyncRepositoryImpl.downloadDossier`), so there is
     * never more than one cached center — this is "the" center, resolved
     * without needing a paper to derive its id from first.
     */
    @Query("SELECT * FROM centers LIMIT 1")
    suspend fun getFirst(): CenterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(center: CenterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(centers: List<CenterEntity>): List<Long>

    @Query("DELETE FROM centers")
    suspend fun clearAll(): Int
}
