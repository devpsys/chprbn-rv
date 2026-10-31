package ng.com.chprbn.mobile.feature.accreditation.presentation

/**
 * Filter chips on Local Drafts. Presentation-only until Room drafts land.
 */
enum class LocalDraftFilter {
    All,
    PendingSync,
    Synced,
    SyncFailed,
}

/**
 * Per-draft sync pill on Local Drafts / Sync Status.
 */
enum class LocalDraftSyncState {
    PendingSync,
    Syncing,
    Synced,
    SyncFailed,
}

data class LocalDraftItemUi(
    val id: String,
    val institutionId: String,
    val institutionName: String,
    val assignmentCode: String,
    val sectionsCompleted: Int,
    val totalSections: Int = 6,
    val attachmentsCount: Int,
    val lastModifiedLabel: String,
    val syncState: LocalDraftSyncState,
    val isSelected: Boolean = false,
    val attemptCount: Int = 0,
    /** Raw error; UI must sanitise via [ng.com.chprbn.mobile.core.network.stripHostsAndUrls]. */
    val lastRawError: String? = null,
)

data class LocalDraftsUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: LocalDraftFilter = LocalDraftFilter.All,
    val drafts: List<LocalDraftItemUi> = emptyList(),
    val visibleDrafts: List<LocalDraftItemUi> = emptyList(),
    val isSyncing: Boolean = false,
    val showDiscardConfirm: Boolean = false,
    val showSyncSuccess: Boolean = false,
    val lastSyncedCount: Int = 0,
) {
    val selectedCount: Int get() = drafts.count { it.isSelected }

    companion object {
        fun placeholder(): LocalDraftsUiState {
            val drafts = placeholderDrafts()
            return LocalDraftsUiState(
                isLoading = false,
                drafts = drafts,
                visibleDrafts = drafts,
            )
        }

        fun placeholderDrafts(): List<LocalDraftItemUi> = listOf(
            LocalDraftItemUi(
                id = "draft-abuth",
                institutionId = "inst-abuth",
                institutionName = "Ahmadu Bello University Teaching Hospital",
                assignmentCode = "CHPRBN-NW-2024-08",
                sectionsCompleted = 5,
                totalSections = 6,
                attachmentsCount = 14,
                lastModifiedLabel = "Today, 10:15 AM",
                syncState = LocalDraftSyncState.PendingSync,
            ),
            LocalDraftItemUi(
                id = "draft-minna",
                institutionId = "inst-minna",
                institutionName = "School of Health Technology, Minna",
                assignmentCode = "CHPRBN-NC-2024-12",
                sectionsCompleted = 3,
                totalSections = 6,
                attachmentsCount = 8,
                lastModifiedLabel = "Yesterday, 04:40 PM",
                syncState = LocalDraftSyncState.SyncFailed,
                attemptCount = 2,
                lastRawError =
                    "Failed to reach https://registry.chprbn.gov.ng:8443/api/v1/sync : " +
                        "SSLHandshakeException at 192.168.1.104",
            ),
            LocalDraftItemUi(
                id = "draft-lagos",
                institutionId = "inst-lagos",
                institutionName = "Lagos State College of Health Technology",
                assignmentCode = "CHPRBN-SW-2024-04",
                sectionsCompleted = 6,
                totalSections = 6,
                attachmentsCount = 19,
                lastModifiedLabel = "22 Oct 2024",
                syncState = LocalDraftSyncState.Synced,
            ),
        )
    }
}

internal fun List<LocalDraftItemUi>.applyDraftFilters(
    query: String,
    filter: LocalDraftFilter,
): List<LocalDraftItemUi> {
    val trimmed = query.trim()
    return filter { draft ->
        val matchesSearch = trimmed.isEmpty() ||
            draft.institutionName.contains(trimmed, ignoreCase = true) ||
            draft.assignmentCode.contains(trimmed, ignoreCase = true)
        val matchesFilter = when (filter) {
            LocalDraftFilter.All -> true
            LocalDraftFilter.PendingSync -> draft.syncState == LocalDraftSyncState.PendingSync ||
                draft.syncState == LocalDraftSyncState.Syncing
            LocalDraftFilter.Synced -> draft.syncState == LocalDraftSyncState.Synced
            LocalDraftFilter.SyncFailed -> draft.syncState == LocalDraftSyncState.SyncFailed
        }
        matchesSearch && matchesFilter
    }
}
