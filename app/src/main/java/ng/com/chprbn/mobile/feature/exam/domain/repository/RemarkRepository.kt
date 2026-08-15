package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult

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

    /**
     * Deletes every remark for [candidateId]. Doesn't touch the sync
     * queue directly — any now-orphaned `SyncJobEntity` self-cleans the
     * next time [ng.com.chprbn.mobile.feature.exam.data.sync.RemarkSyncHandler]
     * finds its local row missing (same pattern as attendance's ghost-job
     * handling).
     */
    suspend fun clearRemarksForCandidate(candidateId: String): SaveResult
}
