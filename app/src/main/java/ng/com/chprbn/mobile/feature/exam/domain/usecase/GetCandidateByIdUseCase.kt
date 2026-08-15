package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import javax.inject.Inject

/** Backs the candidate profile screen, reached from "View Profile" on the roster. */
class GetCandidateByIdUseCase @Inject constructor(
    private val repository: ExamCandidateRepository,
) {
    suspend operator fun invoke(candidateId: String): Candidate? {
        val trimmed = candidateId.trim()
        if (trimmed.isEmpty()) return null
        return repository.getCandidateById(trimmed)
    }
}
