package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.SubmitAssessmentResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentSubmissionRepository
import javax.inject.Inject

class SubmitAssessmentUseCase @Inject constructor(
    private val repository: AssessmentSubmissionRepository,
) {
    suspend operator fun invoke(assessmentId: String): SubmitAssessmentResult =
        repository.submit(assessmentId)
}
