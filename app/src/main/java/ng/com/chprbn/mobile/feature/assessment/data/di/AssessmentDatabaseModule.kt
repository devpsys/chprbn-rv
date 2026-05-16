package ng.com.chprbn.mobile.feature.assessment.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentPaperDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentScheduleDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import javax.inject.Singleton

/**
 * Builds `assessment.db` on top of the shared SQLCipher
 * `SupportOpenHelperFactory` from `core.persistence.encryption.EncryptionModule`,
 * and exposes every DAO as a singleton.
 *
 * Schema version 1; no migrations registered. Future schema bumps MUST add
 * explicit `Migration(n, n+1)` objects to [AssessmentDatabase] and pass them
 * to `addMigrations(...)`. The destructive fallback was deliberately removed —
 * losing pending score writes to a missed migration is unacceptable, so a
 * missing migration now fails loudly at startup instead of silently wiping.
 */
@Module
@InstallIn(SingletonComponent::class)
object AssessmentDatabaseModule {

    @Provides
    @Singleton
    fun provideAssessmentDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportOpenHelperFactory,
    ): AssessmentDatabase =
        Room.databaseBuilder(context, AssessmentDatabase::class.java, "assessment.db")
            .openHelperFactory(supportFactory)
            .build()

    @Provides
    fun provideScheduleDao(db: AssessmentDatabase): AssessmentScheduleDao = db.scheduleDao()

    @Provides
    fun providePaperDao(db: AssessmentDatabase): AssessmentPaperDao = db.paperDao()

    @Provides
    fun provideSectionDao(db: AssessmentDatabase): PracticalSectionDao = db.sectionDao()

    @Provides
    fun provideQuestionDao(db: AssessmentDatabase): SectionQuestionDao = db.questionDao()

    @Provides
    fun provideCandidateDao(db: AssessmentDatabase): AssessmentCandidateDao = db.candidateDao()

    @Provides
    fun providePracticalScoreDao(db: AssessmentDatabase): PracticalScoreDao = db.practicalScoreDao()

    @Provides
    fun provideProjectScoreDao(db: AssessmentDatabase): ProjectScoreDao = db.projectScoreDao()
}
