package ng.com.chprbn.mobile.feature.auth.domain.usecase

import javax.inject.Inject
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return authRepository.login(email = email, password = password)
    }
}

