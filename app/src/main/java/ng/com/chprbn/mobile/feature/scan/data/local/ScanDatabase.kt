package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LicenseRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun licenseRecordDao(): LicenseRecordDao
}
