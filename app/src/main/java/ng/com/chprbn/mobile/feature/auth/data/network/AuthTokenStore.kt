package ng.com.chprbn.mobile.feature.auth.data.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the active Sanctum token for [AuthorizationInterceptor].
 * Updated on successful login and when restoring session from Room; cleared on logout.
 */
@Singleton
class AuthTokenStore @Inject constructor() {

    @Volatile
    private var token: String? = null

    fun setToken(value: String?) {
        token = value
    }

    fun peekToken(): String? = token

    fun clear() {
        token = null
    }
}
