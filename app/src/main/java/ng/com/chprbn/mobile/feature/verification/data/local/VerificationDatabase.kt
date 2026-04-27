package ng.com.chprbn.mobile.feature.verification.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao

@Database(
    entities = [LicenseRecordEntity::class, VerifiedLicenseEntity::class],
    version = 5,
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

        /**
         * Drops `authority`; adds `certificateNo`, `email`, `phone` on license + verified tables
         * (table rebuild — SQLite versions without DROP COLUMN).
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `license_records_new` (
                    `registrationNumber` TEXT NOT NULL,
                    `fullName` TEXT NOT NULL,
                    `photoUrl` TEXT,
                    `profession` TEXT NOT NULL,
                    `licenseStatus` TEXT NOT NULL,
                    `expiryDate` TEXT NOT NULL,
                    `subtitle` TEXT,
                    `issueDate` TEXT NOT NULL,
                    `gender` TEXT NOT NULL,
                    `graduationDate` TEXT NOT NULL,
                    `institutionAttendedName` TEXT,
                    `certificateNo` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    PRIMARY KEY(`registrationNumber`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `license_records_new` (
                    registrationNumber, fullName, photoUrl, profession, licenseStatus, expiryDate, subtitle,
                    issueDate, gender, graduationDate, institutionAttendedName,
                    certificateNo, email, phone
                    ) SELECT
                    registrationNumber, fullName, photoUrl, profession, licenseStatus, expiryDate, subtitle,
                    issueDate, gender, graduationDate, institutionAttendedName,
                    '', '', ''
                    FROM license_records
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE license_records")
                db.execSQL("ALTER TABLE license_records_new RENAME TO license_records")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `verified_licenses_new` (
                    `registrationNumber` TEXT NOT NULL,
                    `fullName` TEXT NOT NULL,
                    `photoUrl` TEXT,
                    `profession` TEXT NOT NULL,
                    `licenseStatus` TEXT NOT NULL,
                    `expiryDate` TEXT NOT NULL,
                    `subtitle` TEXT,
                    `issueDate` TEXT NOT NULL,
                    `gender` TEXT NOT NULL,
                    `graduationDate` TEXT NOT NULL,
                    `institutionAttendedName` TEXT,
                    `certificateNo` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `verificationLocation` TEXT NOT NULL,
                    `practitionerPresent` INTEGER NOT NULL,
                    `remark` TEXT NOT NULL,
                    `verifiedAt` INTEGER NOT NULL,
                    `syncStatus` TEXT NOT NULL,
                    `lastSyncAttempt` INTEGER,
                    `syncError` TEXT,
                    PRIMARY KEY(`registrationNumber`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `verified_licenses_new` (
                    registrationNumber, fullName, photoUrl, profession, licenseStatus, expiryDate, subtitle,
                    issueDate, gender, graduationDate, institutionAttendedName,
                    certificateNo, email, phone,
                    verificationLocation, practitionerPresent, remark, verifiedAt, syncStatus, lastSyncAttempt, syncError
                    ) SELECT
                    registrationNumber, fullName, photoUrl, profession, licenseStatus, expiryDate, subtitle,
                    issueDate, gender, graduationDate, institutionAttendedName,
                    '', '', '',
                    verificationLocation, practitionerPresent, remark, verifiedAt, syncStatus, lastSyncAttempt, syncError
                    FROM verified_licenses
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE verified_licenses")
                db.execSQL("ALTER TABLE verified_licenses_new RENAME TO verified_licenses")
            }
        }
    }
}
