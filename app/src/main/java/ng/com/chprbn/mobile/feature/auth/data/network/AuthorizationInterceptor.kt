package ng.com.chprbn.mobile.feature.auth.data.network

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer` and `Accept: application/json` for mobile API routes
 * except unauthenticated login endpoints.
 */
@Singleton
class AuthorizationInterceptor @Inject constructor(
    private val authTokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.endsWith("/login") || path.endsWith("/auth/login")) {
            return chain.proceed(request)
        }
        val token = authTokenStore.peekToken() ?: return chain.proceed(request)
        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(authenticated)
    }
}
