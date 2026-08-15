package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.DeleteTable

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
 * Schema history:
 * - v1: initial (`assessment_schedules` + package tables).
 * - v2: drops `assessment_schedules`. Schedules are now derived from the
 *   exam-side dossier (PE/PA papers filtered from the `papers` table in
 *   `exam.db`) — no dedicated schedules endpoint, no local cache.
 *   Aggregate sync-status is computed on read in
 *   `AssessmentScheduleRepositoryImpl`.
 */
@Database(
    entities = [
        AssessmentPaperEntity::class,
        PracticalSectionEntity::class,
        SectionQuestionEntity::class,
        AssessmentCandidateEntity::class,
        ScheduleCandidateAssignmentEntity::class,
        PracticalScoreEntity::class,
        ProjectScoreEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AssessmentDatabase.DropSchedulesTableSpec::class),
    ],
)
abstract class AssessmentDatabase : RoomDatabase() {
    abstract fun paperDao(): AssessmentPaperDao
    abstract fun sectionDao(): PracticalSectionDao
    abstract fun questionDao(): SectionQuestionDao
    abstract fun candidateDao(): AssessmentCandidateDao
    abstract fun practicalScoreDao(): PracticalScoreDao
    abstract fun projectScoreDao(): ProjectScoreDao

    /**
     * Room auto-migration spec: v1→v2 drops the removed
     * `assessment_schedules` table (see class KDoc for the rationale).
     */
    @DeleteTable(tableName = "assessment_schedules")
    class DropSchedulesTableSpec : AutoMigrationSpec
}
