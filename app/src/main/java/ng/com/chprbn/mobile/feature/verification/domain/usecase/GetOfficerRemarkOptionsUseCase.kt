package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Use case: fetch the canonical officer-remark choices for the
 * verification form. Returns an empty list on any failure; the
 * presentation layer keeps its bundled defaults in that case.
 */
class GetOfficerRemarkOptionsUseCase @Inject constructor(
    private val repository: VerificationRepository,
) {
    suspend operator fun invoke(): List<String> = repository.getOfficerRemarkOptions()
}
