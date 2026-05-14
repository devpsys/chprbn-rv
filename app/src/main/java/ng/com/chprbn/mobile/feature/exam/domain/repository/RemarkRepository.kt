package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/**
 * Append-only remark surface. Unlike attendance, multiple remarks per
 * candidate coexist, so the repository never REPLACES — it always
 * INSERTs a fresh row keyed by a client-generated UUID.
 */
interface RemarkRepository {

    suspend fun addRemark(
        candidateId: String,
        paperId: String?,
        body: String,
        severity: RemarkSeverity,
    ): AddRemarkResult

    suspend fun getRemarksForCandidate(candidateId: String): List<Remark>
}
