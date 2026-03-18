package ng.com.chprbn.mobile.feature.dashboard.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.dashboard.data.api.DashboardApiService
import ng.com.chprbn.mobile.feature.dashboard.data.repository.DashboardRepositoryImpl
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository

    companion object {
        @Provides
        @Singleton
        fun provideDashboardApiService(retrofit: Retrofit): DashboardApiService =
            retrofit.create(DashboardApiService::class.java)
    }
}
