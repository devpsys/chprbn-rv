package ng.com.chprbn.mobile.feature.auth.data.network

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import ng.com.chprbn.mobile.feature.auth.domain.session.SessionEvent
import ng.com.chprbn.mobile.feature.auth.domain.session.SessionEventBus
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer` and `Accept: application/json` for mobile API routes
 * except unauthenticated login endpoints.
 *
 * If the in-memory token is missing, hydrates from [UserDao] (same source as post-login / splash).
 *
 * On a 401 from any non-login response, clears the token and emits
 * [SessionEvent.Expired] on the [SessionEventBus] so the UI can redirect to
 * login. Login endpoints are skipped before the 401 check, so a 401 from a
 * bad-credentials login attempt is left for the caller to surface.
 */
@Singleton
class AuthorizationInterceptor @Inject constructor(
    private val authTokenStore: AuthTokenStore,
    private val sessionEventBus: SessionEventBus,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        // Tutor: …/login, …/auth/login — Adhoc: …/adhoc/login, …/auth/adhoc/login (all end with /login)
        if (path.endsWith("/login")) {
            return chain.proceed(request)
        }
        val token = resolveBearerToken()
        val finalRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            request
        }
        val response = chain.proceed(finalRequest)
        if (response.code == 401) {
            Log.w(TAG, "401 from $path — clearing token, emitting SessionEvent.Expired")
            authTokenStore.clear()
            sessionEventBus.emit(SessionEvent.Expired)
        }
        return response
    }

    private fun resolveBearerToken(): String? {
        val mem = authTokenStore.peekToken()?.trim()
        if (mem != null && !SessionTokenPolicy.isValidForAuthenticatedApi(mem)) {
            authTokenStore.clear()
            return null
        }
        return mem
    }

    private companion object {
        const val TAG = "SessionExpiry"
    }
}
