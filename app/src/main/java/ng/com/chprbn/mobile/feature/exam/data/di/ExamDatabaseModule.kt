package ng.com.chprbn.mobile.feature.exam.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import javax.inject.Singleton

/**
 * Builds `exam.db` on top of the shared SQLCipher
 * `SupportOpenHelperFactory` from `core.persistence.encryption.EncryptionModule`,
 * and exposes every DAO as a singleton.
 *
 * Schema version 1; no migrations registered. Future schema bumps must
 * add explicit `Migration(n, n+1)` objects to [ExamDatabase] and pass
 * them to `addMigrations(...)`. `fallbackToDestructiveMigration()` is
 * the safety net only — losing pending attendance writes to a missed
 * migration is unacceptable.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExamDatabaseModule {

    @Provides
    @Singleton
    fun provideExamDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportOpenHelperFactory,
    ): ExamDatabase =
        Room.databaseBuilder(context, ExamDatabase::class.java, "exam.db")
            .openHelperFactory(supportFactory)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCenterDao(db: ExamDatabase): CenterDao = db.centerDao()

    @Provides
    fun providePaperDao(db: ExamDatabase): PaperDao = db.paperDao()

    @Provides
    fun provideCandidateDao(db: ExamDatabase): CandidateDao = db.candidateDao()

    @Provides
    fun provideAttendanceDao(db: ExamDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideRemarkDao(db: ExamDatabase): RemarkDao = db.remarkDao()
}
