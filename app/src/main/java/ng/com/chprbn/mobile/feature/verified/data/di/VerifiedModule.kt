package ng.com.chprbn.mobile.feature.verified.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.scan.data.local.ScanDatabase
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verified.data.repository.VerifiedRepositoryImpl
import ng.com.chprbn.mobile.feature.verified.domain.repository.VerifiedRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VerifiedModule {

    @Binds
    abstract fun bindVerifiedRepository(impl: VerifiedRepositoryImpl): VerifiedRepository

    companion object {
        @Provides
        @Singleton
        fun provideVerifiedLicenseDao(db: ScanDatabase): VerifiedLicenseDao =
            db.verifiedLicenseDao()
    }
}

