package ng.com.chprbn.mobile.feature.accreditation.domain.model

data class AssessmentHistoryItem(
    val id: String,
    val cycleLabel: String,
    val dateLabel: String,
    val scoreLabel: String,
    val gradeLabel: String,
    val decisionLabel: String,
)

data class InstitutionProfile(
    val id: String,
    val name: String,
    val ownershipSubtitle: String,
    val photoUrl: String?,
    val syncStatus: InstitutionAssignmentSyncStatus,
    val lastSyncedLabel: String,
    val facilityCode: String,
    val lastVisitLabel: String,
    val ownershipType: String,
    val state: String,
    val zone: String,
    val campusAddress: String,
    val programmes: List<String>,
    val programmesAccreditedCount: Int,
    val academicStaffCount: Int,
    val technicalStaffCount: Int,
    val adminStaffCount: Int,
    val techToAcademicRatio: String,
    val capitalBaseLabel: String,
    val history: List<AssessmentHistoryItem>,
)

sealed interface GetInstitutionProfileResult {
    data class Success(val profile: InstitutionProfile) : GetInstitutionProfileResult
    data class NotFound(val institutionId: String) : GetInstitutionProfileResult
    data class Error(val message: String) : GetInstitutionProfileResult
}
