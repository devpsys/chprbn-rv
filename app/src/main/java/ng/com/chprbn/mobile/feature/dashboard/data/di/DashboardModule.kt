package ng.com.chprbn.mobile.feature.dashboard.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.dashboard.data.repository.DashboardRepositoryImpl
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository
}
