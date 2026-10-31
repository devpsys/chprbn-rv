package ng.com.chprbn.mobile.feature.accreditation.domain.model

enum class LocalDraftSyncState {
    PendingSync,
    Syncing,
    Synced,
    SyncFailed,
}

data class LocalDraftListItem(
    val id: String,
    val institutionId: String,
    val institutionName: String,
    val assignmentCode: String,
    val sectionsCompleted: Int,
    val totalSections: Int = 6,
    val attachmentsCount: Int,
    val lastModifiedLabel: String,
    val syncState: LocalDraftSyncState,
    val attemptCount: Int = 0,
    val lastRawError: String? = null,
)

enum class OverallSyncStatus {
    Synced,
    Syncing,
    SyncFailed,
    Offline,
}

data class FailedDraftDetail(
    val draft: LocalDraftListItem,
)

data class SyncStatusSnapshot(
    val overallStatus: OverallSyncStatus,
    val progressFraction: Float,
    val lastSuccessfulSyncLabel: String,
    val failedDrafts: List<FailedDraftDetail>,
)

sealed interface SyncDraftsResult {
    data class Success(val syncedCount: Int) : SyncDraftsResult
    data class Error(val message: String) : SyncDraftsResult
}
