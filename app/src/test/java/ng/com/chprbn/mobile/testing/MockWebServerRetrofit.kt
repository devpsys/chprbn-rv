package ng.com.chprbn.mobile.testing

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds a Retrofit instance pointed at the given [MockWebServer] using
 * the same Gson + OkHttp config the production graph uses (see
 * `AuthDataModule.provideRetrofit`). Returns the `apiClass` proxy ready
 * for tests to exercise.
 *
 * Used by the JSON-wire integration tests under
 * `app/src/test/.../data/api/`. They spin up a MockWebServer per test
 * to assert the on-the-wire request shape and the envelope parsing
 * round-trip.
 */
internal fun <T : Any> mockServerApi(server: MockWebServer, apiClass: Class<T>): T =
    Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(
            OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build(),
        )
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(apiClass)
