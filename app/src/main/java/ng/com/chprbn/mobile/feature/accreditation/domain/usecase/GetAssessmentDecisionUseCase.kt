package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetDecisionResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentSubmissionRepository
import javax.inject.Inject

class GetAssessmentDecisionUseCase @Inject constructor(
    private val repository: AssessmentSubmissionRepository,
) {
    suspend operator fun invoke(decisionId: String): GetDecisionResult =
        repository.getDecision(decisionId)
}
