package ng.com.chprbn.mobile.feature.accreditation.presentation

enum class OwnershipOption {
    Federal,
    State,
    Lga,
    Private,
    FaithBased,
}

enum class ChecklistAnswer {
    Unanswered,
    Yes,
    No,
    Available,
    NotAvailable,
}

data class SsqAttachmentUiState(
    val id: String,
    val title: String,
    val sizeLabel: String,
    val isPhoto: Boolean,
)

data class PhcTutorUiState(
    val id: String,
    val name: String,
    val roleTitle: String,
    val employmentStatus: String,
    val licenseNo: String,
    val sex: String,
    val qualification: String,
    val phone: String,
)

data class SsqChecklistItemUiState(
    val id: String,
    val text: String,
    val answer: ChecklistAnswer,
    val remarks: String = "",
    val usesAvailability: Boolean = false,
)

data class SectionAFormUiState(
    val ownership: OwnershipOption? = OwnershipOption.Federal,
    val campusLocation: String = "Main Campus, Shika, Zaria",
    val dateOfEstablishment: String = "14 Jan 1967",
    val yearProgrammeCommenced: String = "1975",
    val faculty: String = "College of Health Sciences",
    val department: String = "Community Health",
    val programmesSelected: Set<String> = setOf("BCHS / CHO", "CHEW", "JCHEW"),
    val accreditationStatus: String = "Full Accreditation",
    val dateOfLastVisit: String = "14 Nov 2021",
    val expiryDate: String = "13 Nov 2026",
    val nbteActive: Boolean = true,
    val academicStaff: String = "128",
    val technicalStaff: String = "42",
    val adminStaff: String = "85",
    val tutors: List<PhcTutorUiState> = emptyList(),
)

data class SsqSectionFormData(
    val institutionId: String,
    val sectionKey: SsqSectionKey,
    val title: String,
    val subtitle: String,
    val formCodeLabel: String,
    val status: SectionDraftStatus,
    val sectionA: SectionAFormUiState? = null,
    val checklistItems: List<SsqChecklistItemUiState> = emptyList(),
    val attachments: List<SsqAttachmentUiState> = emptyList(),
    val showSavedDialog: Boolean = false,
)

sealed interface SsqSectionFormUiState {
    data object Loading : SsqSectionFormUiState

    data class Success(val data: SsqSectionFormData) : SsqSectionFormUiState

    data class Error(val message: String) : SsqSectionFormUiState

    companion object {
        private val programmeOptions = listOf(
            "BCHS / CHO",
            "CHEW",
            "HND Health Tech",
            "JCHEW",
        )

        fun programmeOptions(): List<String> = programmeOptions

        fun accreditationStatusOptions(): List<String> = listOf(
            "Full Accreditation",
            "Provisional Accreditation",
            "Denial",
            "Suspension",
            "Others Specify",
        )

        fun placeholder(
            institutionId: String = "inst-abuth",
            sectionKey: SsqSectionKey = SsqSectionKey.A,
        ): Success {
            val id = institutionId.ifBlank { "inst-abuth" }
            val attachments = listOf(
                SsqAttachmentUiState("att-1", "Kaduna_State_Gazette_1998.pdf", "1.4 MB", false),
                SsqAttachmentUiState("att-2", "Campus_Demo_Clinic_Exterior.jpg", "3.8 MB", true),
                SsqAttachmentUiState("att-3", "Skills_Lab_Audit_01.jpg", "2.9 MB", true),
            )
            return Success(
                when (sectionKey) {
                    SsqSectionKey.A -> sectionA(id, attachments)
                    SsqSectionKey.B -> checklistSection(
                        id = id,
                        key = SsqSectionKey.B,
                        title = "Philosophy, Objectives & Governance",
                        subtitle = "B1 philosophy/objectives + B2 governance",
                        formCode = "Standard Form SSQ B1 & B2",
                        status = SectionDraftStatus.SavedLocally,
                        usesAvailability = false,
                        itemTexts = bItems(),
                        attachments = attachments.take(1),
                    )
                    SsqSectionKey.C -> checklistSection(
                        id = id,
                        key = SsqSectionKey.C,
                        title = "Curriculum & Teaching Methods",
                        subtitle = "C1 teaching methods inventory",
                        formCode = "Standard Form SSQ C1",
                        status = SectionDraftStatus.InProgress,
                        usesAvailability = true,
                        itemTexts = cItems(),
                        attachments = attachments.take(2),
                    )
                    SsqSectionKey.D -> checklistSection(
                        id = id,
                        key = SsqSectionKey.D,
                        title = "Staffing & Academic Administration",
                        subtitle = "D1–D3 staffing and qualifications",
                        formCode = "Standard Form SSQ D1–D3",
                        status = SectionDraftStatus.SavedLocally,
                        usesAvailability = false,
                        itemTexts = dItems(),
                        attachments = attachments.take(1),
                    )
                    SsqSectionKey.E -> checklistSection(
                        id = id,
                        key = SsqSectionKey.E,
                        title = "Physical Facilities, Labs & Technology",
                        subtitle = "E1–E3 facilities inventory",
                        formCode = "Standard Form SSQ E1–E3",
                        status = SectionDraftStatus.NotStarted,
                        usesAvailability = true,
                        itemTexts = eItems(),
                        attachments = emptyList(),
                    )
                    SsqSectionKey.F -> checklistSection(
                        id = id,
                        key = SsqSectionKey.F,
                        title = "Affiliated Clinical Facilities & Sites",
                        subtitle = "F1 clinical/community facilities + MoU",
                        formCode = "Standard Form SSQ F1",
                        status = SectionDraftStatus.NotStarted,
                        usesAvailability = true,
                        itemTexts = fItems(),
                        attachments = emptyList(),
                    )
                },
            )
        }

        private fun sectionA(
            id: String,
            attachments: List<SsqAttachmentUiState>,
        ): SsqSectionFormData = SsqSectionFormData(
            institutionId = id,
            sectionKey = SsqSectionKey.A,
            title = "General Information",
            subtitle = "Institutional profile & teaching staff",
            formCodeLabel = "Standard Form SSQ A1 & A2",
            status = SectionDraftStatus.InProgress,
            sectionA = SectionAFormUiState(
                tutors = listOf(
                    PhcTutorUiState(
                        id = "tutor-1",
                        name = "Dr. Aisha Mohammed Garba",
                        roleTitle = "Senior Primary Health Care Tutor",
                        employmentStatus = "Full-Time",
                        licenseNo = "CHPRBN/2012/0488",
                        sex = "Female",
                        qualification = "M.Sc. PH, CHO",
                        phone = "+234 803 441 2980",
                    ),
                    PhcTutorUiState(
                        id = "tutor-2",
                        name = "Mr. Emmanuel Chukwu",
                        roleTitle = "Clinical Instructor / Tutor I",
                        employmentStatus = "Full-Time",
                        licenseNo = "CHPRBN/2016/1129",
                        sex = "Male",
                        qualification = "B.Sc. Comm Health",
                        phone = "+234 802 879 5041",
                    ),
                    PhcTutorUiState(
                        id = "tutor-3",
                        name = "Mrs. Fatima Bello Yakubu",
                        roleTitle = "Adjunct Community Field Supervisor",
                        employmentStatus = "Part-Time",
                        licenseNo = "CHPRBN/2019/3301",
                        sex = "Female",
                        qualification = "PGD Epidem., CHO",
                        phone = "+234 814 309 6712",
                    ),
                ),
            ),
            attachments = attachments,
        )

        private fun checklistSection(
            id: String,
            key: SsqSectionKey,
            title: String,
            subtitle: String,
            formCode: String,
            status: SectionDraftStatus,
            usesAvailability: Boolean,
            itemTexts: List<String>,
            attachments: List<SsqAttachmentUiState>,
        ): SsqSectionFormData = SsqSectionFormData(
            institutionId = id,
            sectionKey = key,
            title = title,
            subtitle = subtitle,
            formCodeLabel = formCode,
            status = status,
            checklistItems = itemTexts.mapIndexed { index, text ->
                val answered = when (status) {
                    SectionDraftStatus.SavedLocally, SectionDraftStatus.Synced -> true
                    SectionDraftStatus.InProgress -> index < itemTexts.size / 2
                    else -> false
                }
                SsqChecklistItemUiState(
                    id = "${key.key}-${index + 1}",
                    text = text,
                    answer = when {
                        !answered -> ChecklistAnswer.Unanswered
                        usesAvailability -> ChecklistAnswer.Available
                        else -> ChecklistAnswer.Yes
                    },
                    usesAvailability = usesAvailability,
                )
            },
            attachments = attachments,
        )

        private fun bItems(): List<String> = listOf(
            "Institutional philosophy documented and displayed",
            "Programme objectives aligned with CHPRBN guidelines",
            "Advisory board constitution available",
            "Governing council minutes retained for last 3 years",
            "Organogram current and signed",
            "Quality assurance policy in force",
            "Student handbook published",
            "Staff code of conduct documented",
            "Grievance redress mechanism active",
            "Community engagement policy available",
            "Statutory charter filed with registry",
            "Accreditation liaison officer designated",
            "Budget allocation for programme visible",
            "Internal audit schedule followed",
            "External examiner reports archived",
            "Curriculum review committee active",
            "Health & safety policy posted",
            "Equal opportunity policy documented",
            "Research ethics board (if applicable)",
            "Records retention schedule enforced",
        )

        private fun cItems(): List<String> = List(18) { index ->
            "Teaching method unit ${index + 1} — available for inspection"
        }

        private fun dItems(): List<String> = listOf(
            "Professor / Chief Tutor rank filled",
            "Senior Tutor cadre meets guideline minimum",
            "Tutor I/II cadre documented",
            "Clinical instructors licensed",
            "All academic staff have current CHPRBN licenses",
            "Qualification matrix matches posted roles",
            "Full-time vs part-time ratios within band",
            "Admin officer designated for programme",
            "Technical staff certificates on file",
            "HR files complete for sampled staff",
            "Continuing professional development logged",
            "Staff appraisal records current",
        )

        private fun eItems(): List<String> = buildList {
            repeat(10) { add("Lecture facility item ${it + 1}") }
            repeat(15) { add("Laboratory item ${it + 1}") }
            repeat(15) { add("Simulation / modern tech item ${it + 1}") }
        }

        private fun fItems(): List<String> = listOf(
            "Primary affiliated teaching hospital",
            "Secondary clinical posting site",
            "PHC centre attachment site",
            "Community posting LGA site",
            "Skills lab partner facility",
            "MoU current for primary site",
            "MoU current for community postings",
        )
    }
}
