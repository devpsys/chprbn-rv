package ng.com.chprbn.mobile.feature.assessment.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.di.JarabawaRetrofit
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Materialises the assessment feature's Retrofit service(s) from the
 * [JarabawaRetrofit]-qualified instance — score push lives on the same
 * jarabawa mobile API as attendance (`docs/mobile-api-guide.html` §6 / §7).
 *
 * There is no longer a per-schedule package-download endpoint — sections
 * and questions arrive on the exam dossier and are persisted by
 * `ExamSyncRepositoryImpl`.
 */
@Module
@InstallIn(SingletonComponent::class)
object AssessmentApiModule {

    @Provides
    @Singleton
    fun provideAssessmentSyncApiService(@JarabawaRetrofit retrofit: Retrofit): AssessmentSyncApiService =
        retrofit.create(AssessmentSyncApiService::class.java)
}
