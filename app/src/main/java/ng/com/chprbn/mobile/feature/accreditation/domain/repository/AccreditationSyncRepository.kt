package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import kotlinx.coroutines.flow.Flow
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncDraftsResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncStatusSnapshot

interface AccreditationSyncRepository {
    fun observeLocalDrafts(): Flow<List<LocalDraftListItem>>

    suspend fun listLocalDrafts(): List<LocalDraftListItem>

    suspend fun syncAll(): SyncDraftsResult

    suspend fun retryFailed(): SyncDraftsResult

    suspend fun getSyncStatus(): SyncStatusSnapshot
}
