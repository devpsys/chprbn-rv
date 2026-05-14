package ng.com.chprbn.mobile.feature.exam.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Materialises the two Retrofit service interfaces from the shared
 * [Retrofit] instance (configured in `AuthDataModule` — auth
 * interceptors, Gson converter, base URL).
 */
@Module
@InstallIn(SingletonComponent::class)
object ExamApiModule {

    @Provides
    @Singleton
    fun provideExamDossierApiService(retrofit: Retrofit): ExamDossierApiService =
        retrofit.create(ExamDossierApiService::class.java)

    @Provides
    @Singleton
    fun provideExamSyncApiService(retrofit: Retrofit): ExamSyncApiService =
        retrofit.create(ExamSyncApiService::class.java)
}
