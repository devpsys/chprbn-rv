package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerifiedRepository
import javax.inject.Inject

/**
 * Use case: fetch locally stored verified licenses.
 */
class GetVerifiedLicensesUseCase @Inject constructor(
    private val repository: VerifiedRepository
) {
    suspend operator fun invoke(): List<VerifiedLicense> = repository.getVerifiedLicenses()
}

