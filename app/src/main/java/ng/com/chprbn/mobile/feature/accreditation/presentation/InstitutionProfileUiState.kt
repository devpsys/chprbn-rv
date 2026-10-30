package ng.com.chprbn.mobile.feature.accreditation.presentation

/**
 * One past accreditation cycle from the server registry. Score / grade /
 * decision are **server-returned labels** — never computed on device.
 */
data class AccreditationHistoryItemUiState(
    val id: String,
    val cycleLabel: String,
    val dateLabel: String,
    val scoreLabel: String,
    val gradeLabel: String,
    val decisionLabel: String,
)

data class InstitutionProfileData(
    val id: String,
    val name: String,
    val ownershipSubtitle: String,
    val photoUrl: String?,
    val syncStatus: InstitutionSyncStatus,
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
    val history: List<AccreditationHistoryItemUiState>,
)

sealed interface InstitutionProfileUiState {
    data object Loading : InstitutionProfileUiState

    data class Success(val institution: InstitutionProfileData) : InstitutionProfileUiState

    data class NotFound(val institutionId: String) : InstitutionProfileUiState

    data class Error(val message: String) : InstitutionProfileUiState

    companion object {
        /**
         * Placeholder matching Prompt B HTML — replaced once a
         * GetInstitutionProfile use case exists.
         */
        fun placeholder(institutionId: String = "inst-abuth"): Success = Success(
            InstitutionProfileData(
                id = institutionId.ifBlank { "inst-abuth" },
                name = "Ahmadu Bello University Teaching Hospital",
                ownershipSubtitle = "Federal Government • Tertiary Healthcare Institution",
                photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC1qI9Ud3fZEDO0iyzmhjCCZVNGnU8kNBgZt9WLsOmq-dc9HdUIODS3YA1blshavLdSvZw1W4_CEVBr5XHZ5-F6mVargiw2VcD97vgnJaynUsU0xOOfiKi1CtV5pMoD4ssLwZ4U1NHfbqHWl-SGbo_P-Z892UwsVEAEXl2ecf5Lps6xClIrHg3zscWYoW27gyYBFn-jpveuZtmFAQFAKZs9V5-Ya5qKD9deyNtAtmp5",
                syncStatus = InstitutionSyncStatus.Synced,
                lastSyncedLabel = "09:30 AM",
                facilityCode = "CHPRBN-NW-2024-08",
                lastVisitLabel = "14 Nov 2021",
                ownershipType = "Federal Government",
                state = "Kaduna State",
                zone = "North West",
                campusAddress = "Main Campus, Shika, Zaria",
                programmes = listOf(
                    "BCHS / CHO",
                    "CHEW",
                    "HND Health Tech",
                    "JCHEW",
                    "ND Health Info Management",
                ),
                programmesAccreditedCount = 5,
                academicStaffCount = 128,
                technicalStaffCount = 42,
                adminStaffCount = 85,
                techToAcademicRatio = "1:3.0",
                capitalBaseLabel = "₦450,000,000",
                history = listOf(
                    AccreditationHistoryItemUiState(
                        id = "hist-2021",
                        cycleLabel = "Cycle 2021 / 2022",
                        dateLabel = "14 Nov 2021",
                        scoreLabel = "84.5%",
                        gradeLabel = "Grade A",
                        decisionLabel = "Full Accreditation (5 Years)",
                    ),
                    AccreditationHistoryItemUiState(
                        id = "hist-2016",
                        cycleLabel = "Cycle 2016 / 2017",
                        dateLabel = "20 Oct 2016",
                        scoreLabel = "78.0%",
                        gradeLabel = "Grade B",
                        decisionLabel = "Full Accreditation (5 Years)",
                    ),
                ),
            ),
        )
    }
}
