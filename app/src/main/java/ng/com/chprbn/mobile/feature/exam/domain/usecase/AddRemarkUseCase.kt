package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import javax.inject.Inject

/**
 * Append a candidate remark. [body] is trimmed and rejected when blank.
 * [paperId] is optional — null means a centre-wide remark not tied to a
 * specific paper.
 */
class AddRemarkUseCase @Inject constructor(
    private val repository: RemarkRepository,
) {
    suspend operator fun invoke(
        candidateId: String,
        paperId: String?,
        body: String,
        severity: RemarkSeverity = RemarkSeverity.Info,
    ): AddRemarkResult {
        val trimmedCandidate = candidateId.trim()
        val trimmedBody = body.trim()
        if (trimmedCandidate.isEmpty()) {
            return AddRemarkResult.Error("Candidate is required.")
        }
        if (trimmedBody.isEmpty()) {
            return AddRemarkResult.Error("Remark cannot be empty.")
        }
        return repository.addRemark(
            candidateId = trimmedCandidate,
            paperId = paperId?.trim()?.takeIf { it.isNotEmpty() },
            body = trimmedBody,
            severity = severity,
        )
    }
}
