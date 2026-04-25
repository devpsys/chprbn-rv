package ng.com.chprbn.mobile.feature.auth.data.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer` and `Accept: application/json` for mobile API routes
 * except unauthenticated login endpoints.
 *
 * If the in-memory token is missing, hydrates from [UserDao] (same source as post-login / splash).
 */
@Singleton
class AuthorizationInterceptor @Inject constructor(
    private val authTokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        // Tutor: …/login, …/auth/login — Adhoc: …/adhoc/login, …/auth/adhoc/login (all end with /login)
        if (path.endsWith("/login")) {
            return chain.proceed(request)
        }
        val token = resolveBearerToken() ?: return chain.proceed(request)
        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(authenticated)
    }

    private fun resolveBearerToken(): String? {
        val mem = authTokenStore.peekToken()?.trim()
        if (mem != null && !SessionTokenPolicy.isValidForAuthenticatedApi(mem)) {
            authTokenStore.clear()
            return null
        }
        return mem
    }
}
