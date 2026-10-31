package ng.com.chprbn.mobile.feature.accreditation.presentation

enum class CriterionMetStatus {
    Met,
    NotMet,
}

enum class CriteriaGroupKind {
    Location,
    HeadOfDepartment,
    HumanResources,
    ExistingDepartments,
    PracticeArea,
    PhysicalFacilities,
    CapitalBase,
    StaffWelfare,
}

data class CriteriaCheckItemUiState(
    val id: String,
    val checked: Boolean,
)

data class CriteriaHrCountUiState(
    val fullTimePhcTutors: String,
    val partTimePhcTutors: String,
    val fullTimeLecturers: String,
    val adminSupport: String,
)

data class CriteriaGroupUiState(
    val kind: CriteriaGroupKind,
    val metStatus: CriterionMetStatus?,
    val checkItems: List<CriteriaCheckItemUiState>,
    val evidenceNotes: String,
    val attachments: List<SsqAttachmentUiState>,
    val hrCounts: CriteriaHrCountUiState? = null,
    val capitalAmountNaira: String = "",
    val statementOfAccountConfirmed: Boolean = false,
    val hodSalaryNaira: String = "",
    val phcTutorWithDegreeSalaryNaira: String = "",
    val phcTutorWithoutDegreeSalaryNaira: String = "",
    val facilitySummaryChips: List<String> = emptyList(),
) {
    val isEvaluated: Boolean
        get() = metStatus != null
}

data class CriteriaChecklistData(
    val institutionId: String,
    val institutionName: String,
    val locationLabel: String,
    val cycleLabel: String,
    val groups: List<CriteriaGroupUiState>,
    val showSavedDialog: Boolean = false,
) {
    val totalCriteria: Int
        get() = groups.size

    val evaluatedCount: Int
        get() = groups.count { it.isEvaluated }

    val progressFraction: Float
        get() = if (totalCriteria == 0) 0f else evaluatedCount.toFloat() / totalCriteria.toFloat()
}

sealed interface CriteriaChecklistUiState {
    data object Loading : CriteriaChecklistUiState

    data class Success(val data: CriteriaChecklistData) : CriteriaChecklistUiState

    data class Error(val message: String) : CriteriaChecklistUiState

    companion object {
        fun placeholder(institutionId: String = "inst-abuth"): Success = Success(
            CriteriaChecklistData(
                institutionId = institutionId.ifBlank { "inst-abuth" },
                institutionName = "Ahmadu Bello University Teaching Hospital",
                locationLabel = "Zaria, Kaduna State",
                cycleLabel = "Accreditation Cycle 2024/2025",
                groups = defaultGroups(),
            ),
        )

        private fun defaultGroups(): List<CriteriaGroupUiState> = listOf(
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.Location,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("loc_rural", true),
                    CriteriaCheckItemUiState("loc_phc", true),
                    CriteriaCheckItemUiState("loc_hectares", true),
                    CriteriaCheckItemUiState("loc_ownership", true),
                ),
                evidenceNotes = "Survey plan No. KD/ZAR/2014-09 verified; 6.2 hectares campus area in Shika.",
                attachments = listOf(
                    SsqAttachmentUiState("a1", "Survey_Deed_Gazette.pdf", "1.2 MB", false),
                    SsqAttachmentUiState("a2", "Campus_Perimeter_Geotag.jpg", "840 KB", true),
                ),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.HeadOfDepartment,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("hod_trained", true),
                    CriteriaCheckItemUiState("hod_postgrad", true),
                ),
                evidenceNotes = "Dr. Aisha Garba, CHO/M.Sc Public Health, CHPRBN/2012/0488 verified.",
                attachments = listOf(
                    SsqAttachmentUiState("a3", "HOD_Senate_Credentials.pdf", "620 KB", false),
                ),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.HumanResources,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("hr_visiting", true),
                    CriteriaCheckItemUiState("hr_admin", true),
                ),
                evidenceNotes = "Departmental staff roll vetted against payroll and active practicing licenses.",
                attachments = listOf(
                    SsqAttachmentUiState("a4", "Staff_Nominal_Roll_2024.pdf", "2.1 MB", false),
                ),
                hrCounts = CriteriaHrCountUiState(
                    fullTimePhcTutors = "8",
                    partTimePhcTutors = "4",
                    fullTimeLecturers = "12",
                    adminSupport = "3",
                ),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.ExistingDepartments,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("dept_two_programmes", true),
                ),
                evidenceNotes = "Concurrent NBTE & allied council accreditations inspected.",
                attachments = emptyList(),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.PracticeArea,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("prac_facilities", true),
                    CriteriaCheckItemUiState("prac_mou", true),
                    CriteriaCheckItemUiState("prac_coordinator", true),
                ),
                evidenceNotes = "MOU with Kaduna State MOH and Sabon Gari LGA inspected.",
                attachments = listOf(
                    SsqAttachmentUiState("a5", "Tripartite_Clinical_MoU.pdf", "980 KB", false),
                ),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.PhysicalFacilities,
                metStatus = null,
                checkItems = emptyList(),
                evidenceNotes = "",
                attachments = emptyList(),
                facilitySummaryChips = listOf(
                    "Admin Block",
                    "Classrooms & Offices",
                    "Student Common Room",
                    "500-Seat Auditorium",
                    "Library (5,000 vols, 30 e-terminals)",
                    "Simulation Lab",
                    "Medical Lab",
                    "15 Functional Toilets",
                    "PHC Clinic",
                    "Recreational",
                    "Hostel",
                    "Computer Lab (75 PCs)",
                ),
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.CapitalBase,
                metStatus = null,
                checkItems = listOf(
                    CriteriaCheckItemUiState("cap_statement", false),
                ),
                evidenceNotes = "",
                attachments = emptyList(),
                capitalAmountNaira = "48000000",
                statementOfAccountConfirmed = false,
            ),
            CriteriaGroupUiState(
                kind = CriteriaGroupKind.StaffWelfare,
                metStatus = CriterionMetStatus.Met,
                checkItems = listOf(
                    CriteriaCheckItemUiState("welfare_schedules", true),
                ),
                evidenceNotes = "",
                attachments = emptyList(),
                hodSalaryNaira = "150000",
                phcTutorWithDegreeSalaryNaira = "145000",
                phcTutorWithoutDegreeSalaryNaira = "110000",
            ),
        )
    }
}
