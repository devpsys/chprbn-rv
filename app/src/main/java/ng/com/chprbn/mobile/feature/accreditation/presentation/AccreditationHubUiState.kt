package ng.com.chprbn.mobile.feature.accreditation.presentation

/**
 * Sync/draft pill on an assigned-institution row. Presentation-only —
 * domain will own the real mapping once the data layer lands.
 */
enum class InstitutionSyncStatus {
    Synced,
    DraftPendingSync,
    SyncFailed,
    NeverSynced,
}

data class OfficerBannerUiState(
    val initials: String,
    val displayName: String,
    val registrationLabel: String,
    val isVerified: Boolean,
)

/**
 * One assigned institution on the hub. [isActive] distinguishes the
 * current assignment (CTA enabled) from an upcoming locked schedule
 * shown in the secondary queue.
 */
data class AssignedInstitutionUiState(
    val id: String,
    val name: String,
    val locationLabel: String,
    val protocolLabel: String,
    val lastSyncedLabel: String,
    val syncStatus: InstitutionSyncStatus,
    val isActive: Boolean = true,
)

data class UpcomingInstitutionUiState(
    val id: String,
    val name: String,
    val scheduleLabel: String,
)

data class FeatureGridUiState(
    val startSubtitle: String,
    val continueSubtitle: String,
    val draftsSubtitle: String,
    val syncSubtitle: String,
    val isOnline: Boolean = true,
)

data class AccreditationHubUiState(
    val isLoading: Boolean = false,
    val officer: OfficerBannerUiState? = null,
    val featureGrid: FeatureGridUiState = FeatureGridUiState(
        startSubtitle = "",
        continueSubtitle = "",
        draftsSubtitle = "",
        syncSubtitle = "",
    ),
    val assignedInstitutions: List<AssignedInstitutionUiState> = emptyList(),
    val upcomingInstitutions: List<UpcomingInstitutionUiState> = emptyList(),
    val errorMessage: String? = null,
) {
    companion object {
        /**
         * Placeholder sample matching the Prompt A HTML mock — replaced
         * once GetAssignedInstitutions (or equivalent) lands.
         */
        fun placeholder(): AccreditationHubUiState = AccreditationHubUiState(
            isLoading = false,
            officer = OfficerBannerUiState(
                initials = "DB",
                displayName = "Insp. Officer: D. Bello",
                registrationLabel = "CHPRBN REG #OFF-9942",
                isVerified = true,
            ),
            featureGrid = FeatureGridUiState(
                startSubtitle = "Protocol 2024/25",
                continueSubtitle = "Resume section 4 of 9",
                draftsSubtitle = "Ready for sync pipeline",
                syncSubtitle = "Last synced 5m ago",
                isOnline = true,
            ),
            assignedInstitutions = listOf(
                AssignedInstitutionUiState(
                    id = "inst-abuth",
                    name = "Ahmadu Bello University Teaching Hospital",
                    locationLabel = "Zaria, Kaduna • North West",
                    protocolLabel = "Accreditation Cycle 2024/2025",
                    lastSyncedLabel = "Today, 09:30 AM",
                    syncStatus = InstitutionSyncStatus.Synced,
                    isActive = true,
                ),
            ),
            upcomingInstitutions = listOf(
                UpcomingInstitutionUiState(
                    id = "inst-niht",
                    name = "National Institute of Health Technology",
                    scheduleLabel = "Scheduled dispatch • Inspection window starts tomorrow",
                ),
            ),
        )
    }
}
