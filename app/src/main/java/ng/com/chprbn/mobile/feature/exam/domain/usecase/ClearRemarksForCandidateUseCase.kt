package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import javax.inject.Inject

/**
 * Wipes every remark logged against a candidate. Destructive — the
 * caller must surface a confirmation dialog before invoking this (same
 * contract as [ClearExamCacheUseCase]).
 */
class ClearRemarksForCandidateUseCase @Inject constructor(
    private val repository: RemarkRepository,
) {
    suspend operator fun invoke(candidateId: String): SaveResult {
        val trimmed = candidateId.trim()
        if (trimmed.isEmpty()) {
            return SaveResult.Error("Candidate is required.")
        }
        return repository.clearRemarksForCandidate(trimmed)
    }
}
