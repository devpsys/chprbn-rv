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
 * Network stack for the exam backend, kept separate from the app-wide
 * client in `AuthDataModule`: no
 * [ng.com.chprbn.mobile.feature.auth.data.network.AuthorizationInterceptor]
 * here — this backend takes no bearer token, only [LocationHeaderInterceptor].
 *
 * Timeouts are deliberately generous. Officers work from rural CBT
 * centres on weak 2G/3G, batched attendance/score uploads carry a body
 * for up to 50 rows, and the server does per-row upsert work before it
 * responds — so a single call routinely stretches past a minute on a
 * slow uplink. The pre-fix defaults (15/30/30) were causing routine
 * "network error" flips that the officer had no recourse for.
 * Balance: [connectTimeout] stays tighter so a truly offline device
 * still fails fast; [callTimeout] caps the outer wait so a stuck
 * connection eventually gives up instead of hanging forever.
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .callTimeout(4, TimeUnit.MINUTES)
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
