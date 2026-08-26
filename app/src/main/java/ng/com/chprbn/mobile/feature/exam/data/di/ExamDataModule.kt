package ng.com.chprbn.mobile.feature.exam.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import ng.com.chprbn.mobile.core.session.SessionScopedCleaner
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncEntityTypeKey
import ng.com.chprbn.mobile.feature.exam.data.repository.AttendanceRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.CachedRecordsRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamCandidateRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamPaperRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamSessionCleaner
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamStatisticsRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.ExamSyncRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.repository.RemarkRepositoryImpl
import ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ApiExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceSyncHandler
import ng.com.chprbn.mobile.feature.exam.data.sync.RemarkSyncHandler
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.CachedRecordsRepository
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
 * - **ExamDossierRemoteSource** — `@Provides` binds
 *   [ApiExamDossierRemoteSource] directly in every build type. The fake
 *   fallback (`FakeExamDossierRemoteSource` / `CompositeExamDossierRemoteSource`)
 *   is intentionally disabled here — it was reachable in debug builds and
 *   made a live-but-center-less API response indistinguishable from a
 *   real download (see `ApiExamDossierRemoteSource`'s doc comment). Both
 *   classes are kept for now but unwired; re-enable only behind an
 *   explicit, non-signing-config-tied dev flag if this is needed again.
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
    abstract fun bindCachedRecordsRepository(
        impl: CachedRecordsRepositoryImpl,
    ): CachedRecordsRepository

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

    /**
     * Contributes the exam-side wipe to the cross-feature `SessionCleaner`
     * multibinding. **Currently unused** — `SessionCleaner` is no longer
     * called from logout (see its doc comment); kept for a possible
     * future "switch account" / "wipe all data" flow.
     */
    @Binds
    @IntoSet
    abstract fun bindExamSessionCleaner(impl: ExamSessionCleaner): SessionScopedCleaner

    companion object {

        /**
         * Always the live API, in every build type — no Fake fallback.
         * A 500 / empty envelope / center-less response surfaces as a
         * real error (or "no dossier" empty state) rather than silently
         * synthetic data, whether this is a release build or a debug
         * build sideloaded for field testing (E1 audit finding, extended
         * to cover debug builds after a field officer saw a fake 3-paper /
         * 3-candidate dossier reported as a real 150-candidate download).
         */
        @Provides
        @Singleton
        fun provideExamDossierRemoteSource(
            api: ApiExamDossierRemoteSource,
        ): ExamDossierRemoteSource = api
    }
}
