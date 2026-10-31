package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetOverviewResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class GetAssessmentOverviewUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(institutionId: String): GetOverviewResult =
        repository.getOverview(institutionId)
}
