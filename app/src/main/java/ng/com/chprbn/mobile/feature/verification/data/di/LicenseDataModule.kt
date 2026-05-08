package ng.com.chprbn.mobile.feature.verification.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import ng.com.chprbn.mobile.feature.verification.data.api.LicenseApiService
import ng.com.chprbn.mobile.feature.verification.data.local.LicenseRecordDao
import ng.com.chprbn.mobile.feature.verification.data.local.VerificationDatabase
import ng.com.chprbn.mobile.feature.verification.data.repository.LicenseRepositoryImpl
import ng.com.chprbn.mobile.feature.verification.data.source.ApiLicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.verification.data.source.CompositeLicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.verification.data.source.FakeLicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.verification.data.source.LicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.repository.LicenseRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LicenseDataModule {

    @Binds
    abstract fun bindLicenseRepository(impl: LicenseRepositoryImpl): LicenseRepository

    companion object {
        @Provides
        @Singleton
        fun provideVerificationDatabase(
            @ApplicationContext context: Context,
            supportFactory: SupportFactory
        ): VerificationDatabase =
            Room.databaseBuilder(context, VerificationDatabase::class.java, "scan.db")
                .openHelperFactory(supportFactory)
                .addMigrations(
                    VerificationDatabase.MIGRATION_2_3,
                    VerificationDatabase.MIGRATION_3_4,
                    VerificationDatabase.MIGRATION_4_5
                )
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @Singleton
        fun provideLicenseRecordDao(db: VerificationDatabase): LicenseRecordDao =
            db.licenseRecordDao()

        @Provides
        @Singleton
        fun provideLicenseApiService(retrofit: Retrofit): LicenseApiService =
            retrofit.create(LicenseApiService::class.java)

        @Provides
        @Singleton
        fun provideLicenseRecordRemoteSource(
            api: ApiLicenseRecordRemoteSource,
            fake: FakeLicenseRecordRemoteSource
        ): LicenseRecordRemoteSource =
            CompositeLicenseRecordRemoteSource(api, fake)
    }
}
