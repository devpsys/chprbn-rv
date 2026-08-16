package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import javax.inject.Inject

/**
 * Snapshot read of a candidate's project score for the
 * `AssessmentProjectAssessment` screen. Returns `null` when the
 * candidate has not been scored yet so the input can stay blank.
 */
class GetProjectScoreUseCase @Inject constructor(
    private val repository: ProjectScoringRepository,
) {
    suspend operator fun invoke(scheduleId: String, candidateId: String): ProjectScore? {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) return null
        return repository.getProjectScore(schedule, candidate)
    }
}
