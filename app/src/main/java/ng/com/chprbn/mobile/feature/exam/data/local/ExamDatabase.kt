package ng.com.chprbn.mobile.feature.exam.data.local

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
 * Schema version 1; no migrations yet. Future schema bumps must add
 * explicit `Migration(n, n+1)` objects here and pass them to
 * `Room.databaseBuilder(...).addMigrations(...)`. `fallbackToDestructive`
 * is the safety net only — losing pending attendance writes to a missed
 * migration is unacceptable.
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
    version = 1,
    exportSchema = false,
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun centerDao(): CenterDao
    abstract fun paperDao(): PaperDao
    abstract fun candidateDao(): CandidateDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun remarkDao(): RemarkDao
}
