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
 * Materialises the two Retrofit service interfaces from the
 * [JarabawaRetrofit]-qualified [Retrofit] instance (configured in
 * [JarabawaNetworkModule] — X-Location interceptor, Gson converter, its own
 * base URL). Exam endpoints live on the jarabawa backend, not the app-wide
 * one in `AuthDataModule`.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExamApiModule {

    @Provides
    @Singleton
    fun provideExamDossierApiService(@JarabawaRetrofit retrofit: Retrofit): ExamDossierApiService =
        retrofit.create(ExamDossierApiService::class.java)

    @Provides
    @Singleton
    fun provideExamSyncApiService(@JarabawaRetrofit retrofit: Retrofit): ExamSyncApiService =
        retrofit.create(ExamSyncApiService::class.java)
}
