package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult

/**
 * One remark per candidate. [addRemark] always REPLACEs whatever remark
 * (if any) is already on file for that candidate, keyed by
 * `candidateId` — it never appends a second row.
 */
interface RemarkRepository {

    suspend fun addRemark(
        candidateId: String,
        paperId: String?,
        body: String,
        severity: RemarkSeverity,
        code: String = "",
    ): AddRemarkResult

    suspend fun getRemarksForCandidate(candidateId: String): List<Remark>

    /**
     * Deletes the remark on file for [candidateId], if any. Doesn't
     * touch the sync queue directly — any now-orphaned `SyncJobEntity`
     * self-cleans the next time
     * [ng.com.chprbn.mobile.feature.exam.data.sync.RemarkSyncHandler]
     * finds its local row missing (same pattern as attendance's ghost-job
     * handling).
     */
    suspend fun clearRemarksForCandidate(candidateId: String): SaveResult
}
