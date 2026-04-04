package ng.com.chprbn.mobile.feature.auth.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult

/**
 * Domain contract for authentication.
 */
interface AuthRepository {
    /** [username] is the practitioner license number (mobile API `username`). */
    suspend fun login(username: String, password: String): AuthResult
}
