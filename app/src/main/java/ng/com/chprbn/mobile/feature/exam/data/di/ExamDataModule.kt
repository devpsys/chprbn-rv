package ng.com.chprbn.mobile.feature.exam.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncEntityTypeKey
import ng.com.chprbn.mobile.feature.exam.data.repository.AttendanceRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamCandidateRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamPaperRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamStatisticsRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamSyncRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.RemarkRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ApiExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.CompositeExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.FakeExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceSyncHandler
import ng.com.chprbn.mobile.feature.exam.data.sync.RemarkSyncHandler
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import javax.inject.Singleton

/**
 * Wires every exam-side abstraction to its concrete implementation:
 *
 * - **Repositories** — domain interfaces ↔ `*Impl` classes.
 * - **ExamDossierRemoteSource** — `@Provides` builds the
 *   [CompositeExamDossierRemoteSource], with the live API as primary
 *   and the in-memory fake as fallback so screens stay functional
 *   before the backend ships.
 * - **ExamSyncRemoteSource** — `@Binds` directly to the Retrofit impl.
 * - **Sync handler multibindings** — both handlers contribute to the
 *   shared `core.sync` `Map<SyncEntityType, SyncEntityHandler>` so
 *   `SyncBatchRunner` dispatches by entity type.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ExamDataModule {

    @Binds
    @Singleton
    abstract fun bindExamPaperRepository(
        impl: ExamPaperRepositoryImpl,
    ): ExamPaperRepository

    @Binds
    @Singleton
    abstract fun bindExamCandidateRepository(
        impl: ExamCandidateRepositoryImpl,
    ): ExamCandidateRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        impl: AttendanceRepositoryImpl,
    ): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindRemarkRepository(
        impl: RemarkRepositoryImpl,
    ): RemarkRepository

    @Binds
    @Singleton
    abstract fun bindExamStatisticsRepository(
        impl: ExamStatisticsRepositoryImpl,
    ): ExamStatisticsRepository

    @Binds
    @Singleton
    abstract fun bindExamSyncRepository(
        impl: ExamSyncRepositoryImpl,
    ): ExamSyncRepository

    @Binds
    @Singleton
    abstract fun bindExamSyncRemoteSource(
        impl: ApiExamSyncRemoteSource,
    ): ExamSyncRemoteSource

    @Binds
    @IntoMap
    @SyncEntityTypeKey(SyncEntityType.Attendance)
    abstract fun bindAttendanceSyncHandler(
        impl: AttendanceSyncHandler,
    ): SyncEntityHandler

    @Binds
    @IntoMap
    @SyncEntityTypeKey(SyncEntityType.Remark)
    abstract fun bindRemarkSyncHandler(
        impl: RemarkSyncHandler,
    ): SyncEntityHandler

    companion object {

        @Provides
        @Singleton
        fun provideExamDossierRemoteSource(
            api: ApiExamDossierRemoteSource,
            fake: FakeExamDossierRemoteSource,
        ): ExamDossierRemoteSource = CompositeExamDossierRemoteSource(api, fake)
    }
}
