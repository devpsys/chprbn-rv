package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetInstitutionProfileResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.InstitutionRepository
import javax.inject.Inject

class GetInstitutionProfileUseCase @Inject constructor(
    private val repository: InstitutionRepository,
) {
    suspend operator fun invoke(institutionId: String): GetInstitutionProfileResult =
        repository.getProfile(institutionId)
}
