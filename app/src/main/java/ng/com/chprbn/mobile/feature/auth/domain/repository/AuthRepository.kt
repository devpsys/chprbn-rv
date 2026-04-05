package ng.com.chprbn.mobile.feature.auth.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult

/**
 * Domain contract for authentication.
 */
interface AuthRepository {
    /** [username] is the adhoc API username (mobile `POST adhoc/login`). */
    suspend fun login(username: String, password: String): AuthResult
}
