package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import javax.inject.Inject

/**
 * Resolves a scanned QR payload (or manually-entered indexing number) to a
 * candidate within a schedule's roster. Returns `null` when the candidate
 * is not in this schedule's local cache — the UI then routes to the
 * "candidate not found" screen.
 */
class LookupAssessmentCandidateUseCase @Inject constructor(
    private val repository: AssessmentCandidateRepository,
) {
    suspend operator fun invoke(scheduleId: String, candidateId: String): Candidate? {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) return null
        return repository.getCandidate(schedule, candidate)
    }
}
