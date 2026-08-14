package ng.com.chprbn.mobile.feature.auth.data.network

import javax.inject.Inject
import javax.inject.Singleton
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `x-location` (the officer's `adhoc/profile` location, e.g.
 * `"mchst213"`) to the exam endpoints that need it to resolve server-side
 * routing/scoping: dossier fetch and the attendance/remarks batch uploads.
 */
@Singleton
class LocationHeaderInterceptor @Inject constructor(
    private val userDao: UserDao,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (LOCATION_ENDPOINTS.none { path.endsWith(it) }) {
            return chain.proceed(request)
        }
        val location = userDao.getUser()?.location
        val finalRequest = if (!location.isNullOrBlank()) {
            request.newBuilder()
                .header("x-location", location)
                .build()
        } else {
            request
        }
        return chain.proceed(finalRequest)
    }

    private companion object {
        val LOCATION_ENDPOINTS = setOf(
            "/attendance/fetch-record",
            "/attendance/push-record",
            "/attendance-remarks",
            "/project/push-record",
            "/practical/push-record",
        )
    }
}
