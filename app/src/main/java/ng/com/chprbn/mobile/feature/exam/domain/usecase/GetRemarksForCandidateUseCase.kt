package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import javax.inject.Inject

class GetRemarksForCandidateUseCase @Inject constructor(
    private val repository: RemarkRepository,
) {
    suspend operator fun invoke(candidateId: String): List<Remark> {
        val trimmed = candidateId.trim()
        if (trimmed.isEmpty()) return emptyList()
        return repository.getRemarksForCandidate(trimmed)
    }
}
