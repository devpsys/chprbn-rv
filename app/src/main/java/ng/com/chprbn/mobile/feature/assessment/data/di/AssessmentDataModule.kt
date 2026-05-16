package ng.com.chprbn.mobile.feature.assessment.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncEntityTypeKey
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentCandidateRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentSyncRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.PracticalScoringRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.ProjectScoringRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.source.ApiAssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.ApiAssessmentSyncRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.CompositeAssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.FakeAssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.sync.PracticalScoreSyncHandler
import ng.com.chprbn.mobile.feature.assessment.data.sync.ProjectScoreSyncHandler
import ng.com.chprbn.mobile.feature.assessment.domain.model.LowScoreThreshold
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentSyncRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import javax.inject.Singleton

/**
 * Wires every assessment-side abstraction to its concrete implementation:
 *
 * - **Repositories** — domain interfaces ↔ `*Impl` classes.
 * - **AssessmentPackageRemoteSource** — `@Provides` builds the
 *   [CompositeAssessmentPackageRemoteSource], with the live API as primary
 *   and the in-memory fake as fallback so screens stay functional before
 *   the backend ships.
 * - **AssessmentSyncRemoteSource** — `@Binds` directly to the Retrofit
 *   impl (no fake — uploading to nowhere is never the right behaviour).
 * - **Sync handler multibindings** — both score handlers contribute to
 *   the shared `core.sync` `Map<SyncEntityType, SyncEntityHandler>` so
 *   `SyncBatchRunner` dispatches by entity type.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AssessmentDataModule {

    @Binds
    @Singleton
    abstract fun bindAssessmentScheduleRepository(
        impl: AssessmentScheduleRepositoryImpl,
    ): AssessmentScheduleRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentCandidateRepository(
        impl: AssessmentCandidateRepositoryImpl,
    ): AssessmentCandidateRepository

    @Binds
    @Singleton
    abstract fun bindPracticalScoringRepository(
        impl: PracticalScoringRepositoryImpl,
    ): PracticalScoringRepository

    @Binds
    @Singleton
    abstract fun bindProjectScoringRepository(
        impl: ProjectScoringRepositoryImpl,
    ): ProjectScoringRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentSyncRepository(
        impl: AssessmentSyncRepositoryImpl,
    ): AssessmentSyncRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentSyncRemoteSource(
        impl: ApiAssessmentSyncRemoteSource,
    ): AssessmentSyncRemoteSource

    @Binds
    @IntoMap
    @SyncEntityTypeKey(SyncEntityType.PracticalScore)
    abstract fun bindPracticalScoreSyncHandler(
        impl: PracticalScoreSyncHandler,
    ): SyncEntityHandler

    @Binds
    @IntoMap
    @SyncEntityTypeKey(SyncEntityType.ProjectScore)
    abstract fun bindProjectScoreSyncHandler(
        impl: ProjectScoreSyncHandler,
    ): SyncEntityHandler

    companion object {

        @Provides
        @Singleton
        fun provideAssessmentPackageRemoteSource(
            api: ApiAssessmentPackageRemoteSource,
            fake: FakeAssessmentPackageRemoteSource,
        ): AssessmentPackageRemoteSource = CompositeAssessmentPackageRemoteSource(api, fake)

        /**
         * Cohort-level "below this aggregate score = Low" threshold. Defaulted
         * to [ScoreLevel.DEFAULT_LOW_THRESHOLD] (50). A future per-cohort /
         * per-schedule override swaps this one binding without touching the
         * use case or mapper layer.
         */
        @Provides
        @LowScoreThreshold
        fun provideLowScoreThreshold(): Int = ScoreLevel.DEFAULT_LOW_THRESHOLD
    }
}
