package ng.com.chprbn.mobile.feature.accreditation.domain.model

data class OfficerBanner(
    val initials: String,
    val displayName: String,
    val registrationLabel: String,
    val isVerified: Boolean,
)

data class FeatureGridInfo(
    val startSubtitle: String,
    val continueSubtitle: String,
    val draftsSubtitle: String,
    val syncSubtitle: String,
    val isOnline: Boolean = true,
)

data class AssignedInstitution(
    val id: String,
    val name: String,
    val locationLabel: String,
    val protocolLabel: String,
    val lastSyncedLabel: String,
    val syncStatus: InstitutionAssignmentSyncStatus,
    val isActive: Boolean = true,
)

data class UpcomingInstitution(
    val id: String,
    val name: String,
    val scheduleLabel: String,
)

data class AccreditationHubData(
    val officer: OfficerBanner,
    val featureGrid: FeatureGridInfo,
    val assignedInstitutions: List<AssignedInstitution>,
    val upcomingInstitutions: List<UpcomingInstitution>,
)

sealed interface GetHubResult {
    data class Success(val data: AccreditationHubData) : GetHubResult
    data class Error(val message: String) : GetHubResult
}
