package ng.com.chprbn.mobile.feature.verification.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.verification.data.local.ScanDatabase
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.repository.VerifiedRepositoryImpl
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerifiedRepository
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

