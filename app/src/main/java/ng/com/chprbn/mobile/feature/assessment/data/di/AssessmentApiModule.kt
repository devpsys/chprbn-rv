package ng.com.chprbn.mobile.feature.assessment.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentPackageApiService
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Materialises the two Retrofit service interfaces from the shared
 * [Retrofit] instance (configured in `AuthDataModule` — auth interceptors,
 * Gson converter, base URL).
 */
@Module
@InstallIn(SingletonComponent::class)
object AssessmentApiModule {

    @Provides
    @Singleton
    fun provideAssessmentPackageApiService(retrofit: Retrofit): AssessmentPackageApiService =
        retrofit.create(AssessmentPackageApiService::class.java)

    @Provides
    @Singleton
    fun provideAssessmentSyncApiService(retrofit: Retrofit): AssessmentSyncApiService =
        retrofit.create(AssessmentSyncApiService::class.java)
}
