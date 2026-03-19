package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LicenseRecordDao {
    @Query("SELECT * FROM license_records WHERE registrationNumber = :registrationNumber LIMIT 1")
    fun getByRegistrationNumber(registrationNumber: String): LicenseRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(record: LicenseRecordEntity): Long
}
