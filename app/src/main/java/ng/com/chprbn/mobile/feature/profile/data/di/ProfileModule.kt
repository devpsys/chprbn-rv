package ng.com.chprbn.mobile.feature.profile.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.feature.profile.data.repository.ProfileRepositoryImpl
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}
