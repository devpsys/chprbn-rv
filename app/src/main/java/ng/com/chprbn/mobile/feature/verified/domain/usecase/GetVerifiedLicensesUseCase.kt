package ng.com.chprbn.mobile.feature.verified.domain.usecase

import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verified.domain.repository.VerifiedRepository
import javax.inject.Inject

/**
 * Use case: fetch locally stored verified licenses.
 */
class GetVerifiedLicensesUseCase @Inject constructor(
    private val repository: VerifiedRepository
) {
    suspend operator fun invoke(): List<VerifiedLicense> = repository.getVerifiedLicenses()
}

