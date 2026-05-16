package ng.com.chprbn.mobile.feature.verification.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.verification.data.api.OfficerRemarkOptionsApiService
import ng.com.chprbn.mobile.feature.verification.data.api.VerificationApiService
import ng.com.chprbn.mobile.feature.verification.data.repository.VerificationRepositoryImpl
import ng.com.chprbn.mobile.feature.verification.data.source.ApiOfficerRemarkOptionsRemoteSource
import ng.com.chprbn.mobile.feature.verification.data.source.OfficerRemarkOptionsRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VerificationModule {

    @Binds
    abstract fun bindVerificationRepository(
        impl: VerificationRepositoryImpl
    ): VerificationRepository

    @Binds
    abstract fun bindOfficerRemarkOptionsRemoteSource(
        impl: ApiOfficerRemarkOptionsRemoteSource
    ): OfficerRemarkOptionsRemoteSource

    companion object {
        @Provides
        @Singleton
        fun provideVerificationApiService(retrofit: Retrofit): VerificationApiService =
            retrofit.create(VerificationApiService::class.java)

        @Provides
        @Singleton
        fun provideOfficerRemarkOptionsApiService(
            retrofit: Retrofit,
        ): OfficerRemarkOptionsApiService =
            retrofit.create(OfficerRemarkOptionsApiService::class.java)
    }
}
