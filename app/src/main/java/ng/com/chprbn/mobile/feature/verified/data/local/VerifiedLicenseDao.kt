package ng.com.chprbn.mobile.feature.verified.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface VerifiedLicenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: VerifiedLicenseEntity): Long

    @Query("SELECT * FROM verified_licenses ORDER BY verifiedAt DESC")
    suspend fun getAll(): List<VerifiedLicenseEntity>

    @Query("SELECT * FROM verified_licenses WHERE licenseStatus = :licenseStatus ORDER BY verifiedAt DESC")
    suspend fun getByLicenseStatus(licenseStatus: String): List<VerifiedLicenseEntity>

    @Query("SELECT * FROM verified_licenses WHERE syncStatus = :syncStatus ORDER BY verifiedAt DESC")
    suspend fun getBySyncStatus(syncStatus: String): List<VerifiedLicenseEntity>

    @Query(
        """
        SELECT * FROM verified_licenses 
        WHERE syncStatus IN ('Pending', 'Failed') 
        ORDER BY verifiedAt DESC
        """
    )
    suspend fun getPendingOrFailed(): List<VerifiedLicenseEntity>

    @Query(
        """
        SELECT * FROM verified_licenses 
        WHERE syncStatus = 'Failed' 
        ORDER BY verifiedAt DESC
        """
    )
    suspend fun getFailed(): List<VerifiedLicenseEntity>

    @Query(
        """
        UPDATE verified_licenses 
        SET syncStatus = :syncStatus, lastSyncAttempt = :lastSyncAttempt, syncError = :syncError 
        WHERE registrationNumber = :registrationNumber
        """
    )
    suspend fun updateSyncMetadata(
        registrationNumber: String,
        syncStatus: String,
        lastSyncAttempt: Long?,
        syncError: String?
    ): Int
}

