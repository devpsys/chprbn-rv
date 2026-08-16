package ng.com.chprbn.mobile.feature.assessment.data.di

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
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentCandidateRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentSessionCleaner
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentSyncRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.PracticalScoringRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.repository.ProjectScoringRepositoryImpl
import ng.com.chprbn.mobile.feature.assessment.data.source.ApiAssessmentSyncRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
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
 * - **AssessmentSyncRemoteSource** — `@Binds` directly to the Retrofit
 *   impl (no fake — uploading to nowhere is never the right behaviour).
 * - **Sync handler multibindings** — both score handlers contribute to
 *   the shared `core.sync` `Map<SyncEntityType, SyncEntityHandler>` so
 *   `SyncBatchRunner` dispatches by entity type.
 *
 * The per-schedule package remote source is gone — practical sections
 * and questions arrive on the exam dossier and are persisted by
 * `ExamSyncRepositoryImpl` in the same download.
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

    /**
     * Contributes the assessment-side wipe to the cross-feature
     * `SessionCleaner` multibinding. **Currently unused** —
     * `SessionCleaner` is no longer called from logout (see its doc
     * comment); kept for a possible future "switch account" / "wipe all
     * data" flow.
     */
    @Binds
    @IntoSet
    abstract fun bindAssessmentSessionCleaner(
        impl: AssessmentSessionCleaner,
    ): SessionScopedCleaner

    companion object {

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
