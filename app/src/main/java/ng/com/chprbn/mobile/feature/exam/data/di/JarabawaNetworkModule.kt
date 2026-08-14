package ng.com.chprbn.mobile.feature.exam.data.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ng.com.chprbn.mobile.BuildConfig
import ng.com.chprbn.mobile.feature.auth.data.network.LocationHeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network stack for the exam backend (jarabawa.chprbn.gov.ng), kept separate
 * from the app-wide client in `AuthDataModule`: no [ng.com.chprbn.mobile.feature.auth.data.network.AuthorizationInterceptor]
 * here — this backend takes no bearer token, only [LocationHeaderInterceptor].
 */
@Module
@InstallIn(SingletonComponent::class)
object JarabawaNetworkModule {

    @Provides
    @Singleton
    @JarabawaRetrofit
    fun provideJarabawaRetrofit(
        locationHeaderInterceptor: LocationHeaderInterceptor,
        gson: Gson,
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(locationHeaderInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                    )
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.JARABAWA_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
