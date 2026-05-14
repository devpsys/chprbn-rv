package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import javax.inject.Inject

class GetExaminationSchedulesUseCase @Inject constructor(
    private val repository: AssessmentScheduleRepository,
) {
    suspend operator fun invoke(): List<AssessmentSchedule> = repository.getSchedules()
}
