package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Encrypted single-feature database for the assessment feature, opened via
 * the shared SQLCipher `SupportOpenHelperFactory` from
 * `core.persistence.encryption.EncryptionModule`. File name: `assessment.db`.
 *
 * Score tables (`practical_scores`, `project_scores`) are intentionally
 * separate from the static reference tables — the package download
 * transaction wipes reference rows but never touches scores, so the
 * examiner's pending writes always survive a re-download.
 *
 * Schema version 1; no migrations yet. Future schema bumps must add
 * explicit `Migration(n, n+1)` objects here and pass them to
 * `Room.databaseBuilder(...).addMigrations(...)`. `fallbackToDestructive`
 * is the safety net only — losing a user's pending scores to a missed
 * migration is unacceptable.
 */
@Database(
    entities = [
        AssessmentScheduleEntity::class,
        AssessmentPaperEntity::class,
        PracticalSectionEntity::class,
        SectionQuestionEntity::class,
        AssessmentCandidateEntity::class,
        ScheduleCandidateAssignmentEntity::class,
        PracticalScoreEntity::class,
        ProjectScoreEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AssessmentDatabase : RoomDatabase() {
    abstract fun scheduleDao(): AssessmentScheduleDao
    abstract fun paperDao(): AssessmentPaperDao
    abstract fun sectionDao(): PracticalSectionDao
    abstract fun questionDao(): SectionQuestionDao
    abstract fun candidateDao(): AssessmentCandidateDao
    abstract fun practicalScoreDao(): PracticalScoreDao
    abstract fun projectScoreDao(): ProjectScoreDao
}
