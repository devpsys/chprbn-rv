package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

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
    version = 3,
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
    ],
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun centerDao(): CenterDao
    abstract fun paperDao(): PaperDao
    abstract fun candidateDao(): CandidateDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun remarkDao(): RemarkDao
}
