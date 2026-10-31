package ng.com.chprbn.mobile.feature.accreditation.presentation

enum class OverallSyncStatusUi {
    Synced,
    Syncing,
    SyncFailed,
    Offline,
}

data class SyncStatusUiState(
    val isLoading: Boolean = false,
    val overallStatus: OverallSyncStatusUi = OverallSyncStatusUi.SyncFailed,
    val progressFraction: Float = 0.67f,
    val lastSuccessfulSyncLabel: String = "Today, 09:30 AM",
    val failedDrafts: List<LocalDraftItemUi> = emptyList(),
    val isRetrying: Boolean = false,
    val showRetrySuccess: Boolean = false,
) {
    companion object {
        fun placeholder(): SyncStatusUiState = SyncStatusUiState(
            isLoading = false,
            overallStatus = OverallSyncStatusUi.SyncFailed,
            progressFraction = 0.67f,
            lastSuccessfulSyncLabel = "Today, 09:30 AM",
            failedDrafts = listOf(
                LocalDraftItemUi(
                    id = "sync-minna",
                    institutionId = "inst-minna",
                    institutionName = "School of Health Technology, Minna",
                    assignmentCode = "CHPRBN-NC-2024-12",
                    sectionsCompleted = 4,
                    totalSections = 6,
                    attachmentsCount = 8,
                    lastModifiedLabel = "Yesterday, 04:40 PM",
                    syncState = LocalDraftSyncState.SyncFailed,
                    attemptCount = 3,
                    lastRawError =
                        "Failed to reach https://registry.chprbn.gov.ng:8443/api/v1/sync : " +
                            "SSLHandshakeException (Peer closed connection at 192.168.1.104)",
                ),
                LocalDraftItemUi(
                    id = "sync-sabon",
                    institutionId = "inst-sabon",
                    institutionName = "College of Health Sciences & Tech, Sabon Gari",
                    assignmentCode = "CHPRBN-NW-2024-19",
                    sectionsCompleted = 6,
                    totalSections = 6,
                    attachmentsCount = 12,
                    lastModifiedLabel = "Today, 08:15 AM",
                    syncState = LocalDraftSyncState.SyncFailed,
                    attemptCount = 2,
                    lastRawError =
                        "Payload validation timeout on endpoint " +
                            "http://gateway.chprbn.gov.ng/upload: Request timeout after 30000ms",
                ),
            ),
        )
    }
}
