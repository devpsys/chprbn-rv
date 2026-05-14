package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import javax.inject.Inject

/**
 * Resolves a scanned QR payload (or manually-entered exam number) to a
 * candidate. Returns `null` when no candidate matches — the
 * scan-result screen routes to the "candidate not found" design in that
 * case.
 */
class LookupCandidateByExamNumberUseCase @Inject constructor(
    private val repository: ExamCandidateRepository,
) {
    suspend operator fun invoke(examNumber: String): Candidate? {
        val trimmed = examNumber.trim()
        if (trimmed.isEmpty()) return null
        return repository.getCandidateByExamNumber(trimmed)
    }
}
