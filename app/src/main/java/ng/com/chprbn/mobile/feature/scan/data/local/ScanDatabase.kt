package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseDao

@Database(
    entities = [LicenseRecordEntity::class, VerifiedLicenseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun licenseRecordDao(): LicenseRecordDao
    abstract fun verifiedLicenseDao(): VerifiedLicenseDao
}
