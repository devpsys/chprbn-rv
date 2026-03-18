package ng.com.chprbn.mobile.feature.auth.domain.model

/**
 * Domain-level authentication result.
 * Keeps domain layer free from Android/Retrofit specifics.
 */
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}

