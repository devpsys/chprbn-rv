package ng.com.chprbn.mobile.feature.verification.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.verification.data.api.VerifiedSyncApiService
import ng.com.chprbn.mobile.feature.verification.data.repository.SyncRepositoryImpl
import ng.com.chprbn.mobile.feature.verification.data.source.ApiVerifiedSyncRemoteSource
import ng.com.chprbn.mobile.feature.verification.data.source.VerifiedSyncRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.repository.SyncRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    @Singleton
    abstract fun bindVerifiedSyncRemoteSource(
        impl: ApiVerifiedSyncRemoteSource
    ): VerifiedSyncRemoteSource

    companion object {
        @Provides
        @Singleton
        fun provideVerifiedSyncApiService(retrofit: Retrofit): VerifiedSyncApiService =
            retrofit.create(VerifiedSyncApiService::class.java)
    }
}
