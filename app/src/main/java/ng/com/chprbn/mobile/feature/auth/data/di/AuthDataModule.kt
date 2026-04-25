package ng.com.chprbn.mobile.feature.auth.data.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ng.com.chprbn.mobile.BuildConfig
import javax.inject.Singleton
import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.local.AuthDatabase
import ng.com.chprbn.mobile.feature.auth.data.local.AuthSeedCallback
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.network.AuthorizationInterceptor
import ng.com.chprbn.mobile.feature.auth.data.repository.AuthRepositoryImpl
import ng.com.chprbn.mobile.feature.auth.data.connectivity.AndroidConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.data.connectivity.ConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AuthDataModule {
    /** Mobile API v1 base (see `mobile_api_v1_documentation.html`). */
    private const val BASE_URL = "https://app.chprbn.gov.ng/api/v1/mobile/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authorizationInterceptor: AuthorizationInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthDatabase(@ApplicationContext context: Context): AuthDatabase =
        Room.databaseBuilder(
            context,
            AuthDatabase::class.java,
            "auth.db"
        ).fallbackToDestructiveMigration()
            .addCallback(AuthSeedCallback())
            .build()

    @Provides
    @Singleton
    fun provideUserDao(db: AuthDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideConnectivityChecker(
        @ApplicationContext context: Context
    ): ConnectivityChecker = AndroidConnectivityChecker(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
