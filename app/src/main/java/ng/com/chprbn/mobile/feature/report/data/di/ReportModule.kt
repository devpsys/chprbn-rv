package ng.com.chprbn.mobile.feature.report.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.report.data.api.IrregularityReportApiService
import ng.com.chprbn.mobile.feature.report.data.repository.IrregularityReportRepositoryImpl
import ng.com.chprbn.mobile.feature.report.domain.repository.IrregularityReportRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportModule {

    @Binds
    @Singleton
    abstract fun bindIrregularityReportRepository(
        impl: IrregularityReportRepositoryImpl
    ): IrregularityReportRepository

    companion object {
        @Provides
        @Singleton
        fun provideIrregularityReportApiService(retrofit: Retrofit): IrregularityReportApiService =
            retrofit.create(IrregularityReportApiService::class.java)
    }
}
