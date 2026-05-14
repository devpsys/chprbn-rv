package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import javax.inject.Inject

class GetAssessmentPaperDetailUseCase @Inject constructor(
    private val repository: AssessmentScheduleRepository,
) {
    suspend operator fun invoke(scheduleId: String): AssessmentPaperDetailResult {
        val trimmed = scheduleId.trim()
        if (trimmed.isEmpty()) {
            return AssessmentPaperDetailResult.Error("Schedule id is required.")
        }
        return repository.getPaperDetail(trimmed)
    }
}
