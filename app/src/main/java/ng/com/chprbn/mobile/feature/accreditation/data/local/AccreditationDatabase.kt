package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Encrypted accreditation draft/attachment store (`accreditation.db`),
 * opened via the shared SQLCipher [net.zetetic.database.sqlcipher.SupportOpenHelperFactory].
 */
@Database(
    entities = [
        AssessmentDraftEntity::class,
        SsqSectionDraftEntity::class,
        CriteriaDraftEntity::class,
        ScoreDraftEntity::class,
        AccreditationAttachmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AccreditationDatabase : RoomDatabase() {
    abstract fun assessmentDraftDao(): AssessmentDraftDao
    abstract fun ssqSectionDraftDao(): SsqSectionDraftDao
    abstract fun criteriaDraftDao(): CriteriaDraftDao
    abstract fun scoreDraftDao(): ScoreDraftDao
    abstract fun accreditationAttachmentDao(): AccreditationAttachmentDao
}
