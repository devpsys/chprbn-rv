package ng.com.chprbn.mobile.feature.auth.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult

/**
 * Domain contract for authentication.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
}

