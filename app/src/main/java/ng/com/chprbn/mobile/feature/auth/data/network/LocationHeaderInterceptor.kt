package ng.com.chprbn.mobile.feature.auth.data.network

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `x-location` (the officer's `adhoc/profile` location, e.g.
 * `"mchst213"`) to every request. Lives only on the jarabawa client
 * (`JarabawaNetworkModule`) — that backend takes no bearer token, so
 * `x-location` is its sole request credential.
 *
 * Fails fast with [IOException] when no location is cached rather than
 * silently sending a credential-less request the server can only answer
 * with an opaque 401 — every `Api*RemoteSource` on the jarabawa client
 * already catches [IOException] and surfaces its message to the UI, so
 * this turns "download failed, no idea why" into an actionable error.
 */
@Singleton
class LocationHeaderInterceptor @Inject constructor(
    private val userDao: UserDao,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val location = userDao.getUser()?.location
        if (location.isNullOrBlank()) {
            throw IOException(
                "No location on file for this account — sign out and back in to refresh your profile."
            )
        }
        val request = chain.request().newBuilder()
            .header("x-location", location)
            .build()
        return chain.proceed(request)
    }
}
