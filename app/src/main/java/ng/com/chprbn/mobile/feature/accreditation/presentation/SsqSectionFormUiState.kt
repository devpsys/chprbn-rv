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
    /** Subsection key such as `B1` / `B2` when the section is grouped. */
    val subsectionId: String = "",
    val numberInSubsection: Int = 0,
)

/**
 * Metadata for a labeled subsection (e.g. SSQ B1 / B2) with its own
 * attachment area. Items live on [SsqSectionFormData.checklistItems]
 * and are filtered by [id].
 */
data class SsqChecklistSubsectionUiState(
    val id: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val tabLabel: String,
    val attachmentsTitle: String,
    val attachmentsHint: String,
    val attachments: List<SsqAttachmentUiState> = emptyList(),
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
    val checklistSubsections: List<SsqChecklistSubsectionUiState> = emptyList(),
    val attachments: List<SsqAttachmentUiState> = emptyList(),
    val showSavedDialog: Boolean = false,
) {
    val evaluatedItemCount: Int
        get() = checklistItems.count { it.answer != ChecklistAnswer.Unanswered }

    val totalChecklistCount: Int
        get() = checklistItems.size
}

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
                    SsqSectionKey.B -> sectionB(id)
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

        private fun sectionB(id: String): SsqSectionFormData {
            // Authoritative item text from docs/accrd/SSQ.pdf — Section B.
            val b1Texts = listOf(
                "Clearly stated philosophy aligned with national health goals",
                "Philosophy aligned with Universal Health Coverage (UHC) agenda",
                "Objectives aligned with Primary Health Care principles",
                "Objectives reflect competency-based education",
                "Objectives address digital health competencies",
                "Objectives include interprofessional education",
                "Objectives include research and innovation",
                "Programme aligned with CHPRBN Act and NUC CCMAS",
                "Institutional strategic plan supports programme sustainability",
                "Quality assurance framework available",
            )
            // B2 items 1–2 are Available / Not Available; 3–10 are Yes / No (SSQ.pdf).
            val b2Items = listOf(
                "Functional departmental board" to true,
                "Functional faculty board" to true,
                "Crèche facility available" to false,
                "Programme coordinator appointed" to false,
                "Student handbook available" to false,
                "Academic policies available" to false,
                "Examination policy available" to false,
                "Staff appraisal system available" to false,
                "Anti-sexual harassment policy available" to false,
                "ICT governance and e-learning policy available" to false,
            )
            // Placeholder answers: 3 of 20 evaluated (matches design progress sample).
            val answeredIds = setOf("B1-1", "B1-3", "B2-1")
            val items = buildList {
                b1Texts.forEachIndexed { index, text ->
                    val itemId = "B1-${index + 1}"
                    add(
                        SsqChecklistItemUiState(
                            id = itemId,
                            text = text,
                            answer = if (itemId in answeredIds) {
                                ChecklistAnswer.Yes
                            } else {
                                ChecklistAnswer.Unanswered
                            },
                            remarks = when (itemId) {
                                "B1-1" ->
                                    "Current vision displayed in Academic Catalog pg. 4-6 " +
                                        "explicitly mirrors FMOH Primary Healthcare Guideline 2023."
                                "B1-3" ->
                                    "Triennial curriculum review documented in 2022 Senate Digest."
                                else -> ""
                            },
                            usesAvailability = false,
                            subsectionId = "B1",
                            numberInSubsection = index + 1,
                        ),
                    )
                }
                b2Items.forEachIndexed { index, (text, usesAvailability) ->
                    val itemId = "B2-${index + 1}"
                    add(
                        SsqChecklistItemUiState(
                            id = itemId,
                            text = text,
                            answer = when {
                                itemId !in answeredIds -> ChecklistAnswer.Unanswered
                                usesAvailability -> ChecklistAnswer.Available
                                else -> ChecklistAnswer.Yes
                            },
                            remarks = if (itemId == "B2-1") {
                                "Board inaugurated under Ministry of Health Order No. 44; " +
                                    "7 accredited external members."
                            } else {
                                ""
                            },
                            usesAvailability = usesAvailability,
                            subsectionId = "B2",
                            numberInSubsection = index + 1,
                        ),
                    )
                }
            }
            return SsqSectionFormData(
                institutionId = id,
                sectionKey = SsqSectionKey.B,
                title = "Philosophy & Governance",
                subtitle = "Regulatory verification checklist for Primary Health Care institutional compliance.",
                formCodeLabel = "CHPRBN SSQ Accreditation",
                status = SectionDraftStatus.InProgress,
                checklistItems = items,
                checklistSubsections = listOf(
                    SsqChecklistSubsectionUiState(
                        id = "B1",
                        badge = "B1",
                        title = "Institutional Philosophy & Educational Objectives",
                        subtitle = "Curricular framing & graduate capacity standard",
                        tabLabel = "B1: Objectives",
                        attachmentsTitle = "Upload Evidence & Minutes (B1)",
                        attachmentsHint =
                            "Provide scanned institutional charter, published prospectus, or board sign-off records.",
                        attachments = listOf(
                            SsqAttachmentUiState(
                                id = "b1-att-1",
                                title = "Mission_Charter.jpg",
                                sizeLabel = "1.2 MB",
                                isPhoto = true,
                            ),
                            SsqAttachmentUiState(
                                id = "b1-att-2",
                                title = "Handbook_2024.pdf",
                                sizeLabel = "2.4 MB",
                                isPhoto = false,
                            ),
                        ),
                    ),
                    SsqChecklistSubsectionUiState(
                        id = "B2",
                        badge = "B2",
                        title = "Institutional Governance & Administration",
                        subtitle = "Administrative bodies, leadership, and operational budgets",
                        tabLabel = "B2: Governance",
                        attachmentsTitle = "Upload Evidence & Minutes (B2)",
                        attachmentsHint =
                            "Attach meeting extracts, signed appointment letters, or statutory ledger summaries.",
                        attachments = listOf(
                            SsqAttachmentUiState(
                                id = "b2-att-1",
                                title = "Senate_Minutes_Q1.pdf",
                                sizeLabel = "860 KB",
                                isPhoto = false,
                            ),
                        ),
                    ),
                ),
            )
        }

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
