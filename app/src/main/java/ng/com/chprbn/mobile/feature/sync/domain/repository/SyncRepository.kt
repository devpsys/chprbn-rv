package ng.com.chprbn.mobile.feature.sync.domain.repository

import ng.com.chprbn.mobile.feature.sync.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.sync.domain.model.SyncRecord

interface SyncRepository {
    suspend fun getSyncRecords(): List<SyncRecord>

    /** Uploads each local row that is pending sync or previously failed. */
    suspend fun syncAllPendingAndFailed(): SyncBatchResult

    /** Re-attempts only rows in [Failed] state. */
    suspend fun retryFailed(): SyncBatchResult
}
