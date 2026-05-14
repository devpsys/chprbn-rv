package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

class GetPracticalSectionsUseCase @Inject constructor(
    private val repository: PracticalScoringRepository,
) {
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalSectionSummary> {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) return emptyList()
        return repository.getSections(schedule, candidate)
    }
}
