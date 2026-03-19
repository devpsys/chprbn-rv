package ng.com.chprbn.mobile.feature.scan.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.scan.data.api.ScanApiService
import ng.com.chprbn.mobile.feature.scan.data.local.LicenseRecordDao
import ng.com.chprbn.mobile.feature.scan.data.local.ScanDatabase
import ng.com.chprbn.mobile.feature.scan.data.repository.ScanRepositoryImpl
import ng.com.chprbn.mobile.feature.scan.domain.repository.ScanRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    @Binds
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    companion object {
        @Provides
        @Singleton
        fun provideScanDatabase(@ApplicationContext context: Context): ScanDatabase =
            Room.databaseBuilder(context, ScanDatabase::class.java, "scan.db")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @Singleton
        fun provideLicenseRecordDao(db: ScanDatabase): LicenseRecordDao =
            db.licenseRecordDao()

        @Provides
        @Singleton
        fun provideScanApiService(retrofit: Retrofit): ScanApiService =
            retrofit.create(ScanApiService::class.java)
    }
}
