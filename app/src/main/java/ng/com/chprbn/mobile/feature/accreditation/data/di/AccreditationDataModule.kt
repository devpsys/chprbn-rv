package ng.com.chprbn.mobile.feature.accreditation.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncEntityTypeKey
import ng.com.chprbn.mobile.feature.accreditation.data.api.AccreditationApiService
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationDatabase
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.repository.AccreditationAssignmentRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.repository.AccreditationAttachmentRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.repository.AccreditationSyncRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.repository.AssessmentDraftRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.repository.AssessmentSubmissionRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.repository.InstitutionRepositoryImpl
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationAssignmentRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.ApiAccreditationRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.AssessmentSubmissionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.CompositeAccreditationAssignmentRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.CompositeAccreditationDraftRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.CompositeAccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.CompositeAssessmentSubmissionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.CompositeInstitutionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.FakeAccreditationRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.InstitutionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.sync.AccreditationDraftSyncHandler
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAssignmentRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAttachmentRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationSyncRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentSubmissionRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.InstitutionRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccreditationDataModule {

    @Binds
    abstract fun bindAssignmentRepository(
        impl: AccreditationAssignmentRepositoryImpl,
    ): AccreditationAssignmentRepository

    @Binds
    abstract fun bindInstitutionRepository(
        impl: InstitutionRepositoryImpl,
    ): InstitutionRepository

    @Binds
    abstract fun bindAssessmentDraftRepository(
        impl: AssessmentDraftRepositoryImpl,
    ): AssessmentDraftRepository

    @Binds
    abstract fun bindAttachmentRepository(
        impl: AccreditationAttachmentRepositoryImpl,
    ): AccreditationAttachmentRepository

    @Binds
    abstract fun bindSyncRepository(
        impl: AccreditationSyncRepositoryImpl,
    ): AccreditationSyncRepository

    @Binds
    abstract fun bindSubmissionRepository(
        impl: AssessmentSubmissionRepositoryImpl,
    ): AssessmentSubmissionRepository

    @Binds
    @IntoMap
    @SyncEntityTypeKey(SyncEntityType.AccreditationDraft)
    abstract fun bindAccreditationDraftSyncHandler(
        impl: AccreditationDraftSyncHandler,
    ): SyncEntityHandler

    companion object {
        @Provides
        @Singleton
        fun provideAccreditationDatabase(
            @ApplicationContext context: Context,
            supportFactory: SupportOpenHelperFactory,
        ): AccreditationDatabase =
            Room.databaseBuilder(context, AccreditationDatabase::class.java, "accreditation.db")
                .openHelperFactory(supportFactory)
                .build()

        @Provides
        fun provideAssessmentDraftDao(db: AccreditationDatabase): AssessmentDraftDao =
            db.assessmentDraftDao()

        @Provides
        fun provideSsqSectionDraftDao(db: AccreditationDatabase): SsqSectionDraftDao =
            db.ssqSectionDraftDao()

        @Provides
        fun provideCriteriaDraftDao(db: AccreditationDatabase): CriteriaDraftDao =
            db.criteriaDraftDao()

        @Provides
        fun provideScoreDraftDao(db: AccreditationDatabase): ScoreDraftDao =
            db.scoreDraftDao()

        @Provides
        fun provideAccreditationAttachmentDao(db: AccreditationDatabase): AccreditationAttachmentDao =
            db.accreditationAttachmentDao()

        @Provides
        @Singleton
        fun provideAccreditationApiService(retrofit: Retrofit): AccreditationApiService =
            retrofit.create(AccreditationApiService::class.java)

        @Provides
        @Singleton
        fun provideAssignmentRemoteSource(
            api: ApiAccreditationRemoteSource,
            fake: FakeAccreditationRemoteSource,
        ): AccreditationAssignmentRemoteSource =
            CompositeAccreditationAssignmentRemoteSource(api, fake)

        @Provides
        @Singleton
        fun provideInstitutionRemoteSource(
            api: ApiAccreditationRemoteSource,
            fake: FakeAccreditationRemoteSource,
        ): InstitutionRemoteSource =
            CompositeInstitutionRemoteSource(api, fake)

        @Provides
        @Singleton
        fun provideDraftRemoteSource(
            api: ApiAccreditationRemoteSource,
            fake: FakeAccreditationRemoteSource,
        ): AccreditationDraftRemoteSource =
            CompositeAccreditationDraftRemoteSource(api, fake)

        @Provides
        @Singleton
        fun provideSyncRemoteSource(
            api: ApiAccreditationRemoteSource,
            fake: FakeAccreditationRemoteSource,
        ): AccreditationSyncRemoteSource =
            CompositeAccreditationSyncRemoteSource(api, fake)

        @Provides
        @Singleton
        fun provideSubmissionRemoteSource(
            api: ApiAccreditationRemoteSource,
            fake: FakeAccreditationRemoteSource,
        ): AssessmentSubmissionRemoteSource =
            CompositeAssessmentSubmissionRemoteSource(api, fake)
    }
}
