package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseDao

@Database(
    entities = [LicenseRecordEntity::class, VerifiedLicenseEntity::class],
    version = 4,
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

        /** Adds issue date, gender, graduation date, and institution name to license + verified rows. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE license_records ADD COLUMN issueDate TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE license_records ADD COLUMN gender TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE license_records ADD COLUMN graduationDate TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE license_records ADD COLUMN institutionAttendedName TEXT"
                )
                db.execSQL(
                    "ALTER TABLE verified_licenses ADD COLUMN issueDate TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE verified_licenses ADD COLUMN gender TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE verified_licenses ADD COLUMN graduationDate TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE verified_licenses ADD COLUMN institutionAttendedName TEXT"
                )
            }
        }
    }
}
