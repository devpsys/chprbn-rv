package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Encrypted single-feature database for the exam feature, opened via the
 * shared SQLCipher `SupportOpenHelperFactory` from
 * `core.persistence.encryption.EncryptionModule`. File name: `exam.db`.
 *
 * The local-write tables (`attendance`, `remarks`) are intentionally
 * separate from the static reference tables — the dossier download
 * transaction wipes reference rows but never touches attendance / remarks,
 * so the officer's pending writes always survive a re-download.
 *
 * Future schema bumps must add explicit `Migration`/`AutoMigration`
 * objects here and pass them to `Room.databaseBuilder(...)`.
 * `fallbackToDestructive` is deliberately not used — losing pending
 * attendance writes to a missed migration is unacceptable.
 */
@Database(
    entities = [
        CenterEntity::class,
        PaperEntity::class,
        CandidateEntity::class,
        PaperCandidateAssignmentEntity::class,
        AttendanceEntity::class,
        RemarkEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        // v1 → v2: adds nullable-with-default `centers.hasSections`,
        // derived from the dossier's `data.sections` array.
        AutoMigration(from = 1, to = 2),
        // v2 → v3: adds `centers.year` and `paper_candidate_assignments.
        // scheduledCandidateId`/`scheduleId` — required to push attendance
        // per the confirmed live contract (`docs/mobile-api-guide.html`
        // §4/§5). Both tables are fully wiped and rebuilt on every dossier
        // download, so the defaulted/null values on pre-existing rows are
        // only ever transiently stale until the next download.
        AutoMigration(from = 2, to = 3),
        // v3 → v4 is MIGRATION_3_4 below (not an AutoMigration) — it
        // changes `remarks`' primary key, which AutoMigration can't express.
    ],
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun centerDao(): CenterDao
    abstract fun paperDao(): PaperDao
    abstract fun candidateDao(): CandidateDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun remarkDao(): RemarkDao

    companion object {
        /**
         * v3 → v4: one remark per candidate now (`docs` — officer
         * corrections replace rather than append) — `remarks.candidateId`
         * becomes the primary key and a `code` column is added to carry
         * [ng.com.chprbn.mobile.feature.exam.presentation.RemarkType.code]
         * for `attendance/push-record`'s `remark` field. Unlike the
         * wiped-and-rebuilt reference tables, `remarks` holds pending user
         * writes, so this can't be a destructive fallback: for any
         * candidate with more than one pre-migration row, only the most
         * recent (by `createdAt`) survives — SQLite's bare-column-with-MAX
         * behavior picks the rest of that row's columns to match. Surviving
         * rows get `code = ''` (pre-migration remarks never captured one);
         * [ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceSyncHandler]
         * treats that the same as "no remark on file".
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `remarks_new` (
                        `candidateId` TEXT NOT NULL,
                        `id` TEXT NOT NULL,
                        `paperId` TEXT,
                        `code` TEXT NOT NULL DEFAULT '',
                        `body` TEXT NOT NULL,
                        `severity` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        `syncError` TEXT,
                        `lastSyncAttemptAt` INTEGER,
                        PRIMARY KEY(`candidateId`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `remarks_new`
                        (`candidateId`, `id`, `paperId`, `code`, `body`, `severity`, `createdAt`, `syncStatus`, `syncError`, `lastSyncAttemptAt`)
                    SELECT `candidateId`, `id`, `paperId`, '', `body`, `severity`, MAX(`createdAt`), `syncStatus`, `syncError`, `lastSyncAttemptAt`
                    FROM `remarks`
                    GROUP BY `candidateId`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `remarks`")
                db.execSQL("ALTER TABLE `remarks_new` RENAME TO `remarks`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_remarks_syncStatus` ON `remarks` (`syncStatus`)")
            }
        }
    }
}
