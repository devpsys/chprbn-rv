package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetHubResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAssignmentRepository
import javax.inject.Inject

class GetAccreditationHubUseCase @Inject constructor(
    private val repository: AccreditationAssignmentRepository,
) {
    suspend operator fun invoke(): GetHubResult = repository.getHub()
}
