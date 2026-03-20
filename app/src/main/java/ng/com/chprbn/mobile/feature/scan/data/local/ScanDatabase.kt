package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseDao

@Database(
    entities = [LicenseRecordEntity::class, VerifiedLicenseEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun licenseRecordDao(): LicenseRecordDao
    abstract fun verifiedLicenseDao(): VerifiedLicenseDao

    companion object {
        /** Adds sync retry metadata columns to [VerifiedLicenseEntity]. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE verified_licenses ADD COLUMN lastSyncAttempt INTEGER")
                db.execSQL("ALTER TABLE verified_licenses ADD COLUMN syncError TEXT")
            }
        }
    }
}
