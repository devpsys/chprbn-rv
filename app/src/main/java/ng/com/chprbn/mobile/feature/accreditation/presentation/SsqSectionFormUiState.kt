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
    val sex: String,
    val licenseNo: String,
    val phone: String,
    val qualification: String,
    val employmentStatus: String,
    /** Optional display title; not an SSQ A2 column. */
    val roleTitle: String = "",
)

/** SSQ A1.11–A1.15 programme/level count grids keyed as `group|level`. */
object SsqA1StudentGrids {
    val approvedBchsLevels = listOf("300L", "400L", "500L")

    val indexedUnindexedGroups: List<Pair<String, List<String>>> = listOf(
        "BCHS" to listOf("300L", "400L", "500L"),
        "CHOs" to listOf("100L", "200L"),
        "CHEWs" to listOf("100L", "200L", "300L"),
        "JCHEWs" to listOf("100L", "200L"),
    )

    val enrolledLevels = listOf("100L", "200L")

    fun key(group: String, level: String): String = "$group|$level"

    fun emptyGroupMap(
        groups: List<Pair<String, List<String>>> = indexedUnindexedGroups,
    ): Map<String, String> = groups
        .flatMap { (group, levels) -> levels.map { level -> key(group, level) to "" } }
        .toMap()

    fun emptyApprovedMap(): Map<String, String> =
        approvedBchsLevels.associateWith { "" }

    fun emptyEnrolledMap(): Map<String, String> =
        enrolledLevels.associateWith { "" }
}

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

/** SSQ D1 / D3 staffing table row (required / available counts + remarks). */
data class StaffingRowUiState(
    val id: String,
    val label: String,
    val required: String = "",
    val available: String = "",
    val fullTime: String = "",
    val partTime: String = "",
    val remarks: String = "",
    /** D1 includes Full-Time / Part-Time; D3 does not. */
    val includesEmploymentSplit: Boolean = false,
)

data class SectionDFormUiState(
    val academicRows: List<StaffingRowUiState> = emptyList(),
    val supportRows: List<StaffingRowUiState> = emptyList(),
) {
    val academicAvailableTotal: Int
        get() = academicRows.sumOf { it.available.toIntOrNull() ?: 0 }

    val supportAvailableTotal: Int
        get() = supportRows.sumOf { it.available.toIntOrNull() ?: 0 }
}

data class SectionAFormUiState(
    val ownership: OwnershipOption? = OwnershipOption.Federal,
    val campusLocation: String = "Main Campus, Shika, Zaria",
    val dateOfEstablishment: String = "14 Jan 1967",
    val yearProgrammeCommenced: String = "1975",
    val faculty: String = "College of Health Sciences",
    val department: String = "Community Health",
    /** SSQ A1.4: BCHS, CHO, CHEW / HND, JCHEW / ND. */
    val programmesSelected: Set<String> = setOf("BCHS", "CHO", "CHEW / HND"),
    val accreditationStatus: String = "Full",
    val dateOfLastVisit: String = "14 Nov 2021",
    val expiryDate: String = "13 Nov 2026",
    /** SSQ A1.11 — Approved Number of Students (BCHS levels). */
    val approvedStudents: Map<String, String> = mapOf(
        "300L" to "120",
        "400L" to "110",
        "500L" to "95",
    ),
    /** SSQ A1.12 — keys `programme|level`. */
    val indexedStudents: Map<String, String> = SsqA1StudentGrids.emptyGroupMap().toMutableMap().apply {
        put(SsqA1StudentGrids.key("BCHS", "300L"), "98")
        put(SsqA1StudentGrids.key("BCHS", "400L"), "88")
        put(SsqA1StudentGrids.key("CHOs", "100L"), "42")
        put(SsqA1StudentGrids.key("CHEWs", "100L"), "36")
    },
    /** SSQ A1.13 — keys `programme|level`. */
    val unindexedStudents: Map<String, String> = SsqA1StudentGrids.emptyGroupMap().toMutableMap().apply {
        put(SsqA1StudentGrids.key("BCHS", "300L"), "6")
        put(SsqA1StudentGrids.key("CHEWs", "200L"), "4")
    },
    /** SSQ A1.14 NBTE Accreditation. */
    val nbteNd: Boolean = true,
    val nbteHnd: Boolean = false,
    /** SSQ A1.15 — Number of Students Enrolled (ND / HND). */
    val enrolledNd: Map<String, String> = mapOf("100L" to "65", "200L" to "58"),
    val enrolledHnd: Map<String, String> = mapOf("100L" to "45", "200L" to "40"),
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
    val sectionD: SectionDFormUiState? = null,
    val checklistItems: List<SsqChecklistItemUiState> = emptyList(),
    val checklistSubsections: List<SsqChecklistSubsectionUiState> = emptyList(),
    val attachments: List<SsqAttachmentUiState> = emptyList(),
    val showSavedDialog: Boolean = false,
) {
    val evaluatedItemCount: Int
        get() {
            val checklist = checklistItems.count { it.answer != ChecklistAnswer.Unanswered }
            val sectionD = this.sectionD ?: return checklist
            val rows = sectionD.academicRows.count { it.available.isNotBlank() } +
                sectionD.supportRows.count { it.available.isNotBlank() }
            return checklist + rows
        }

    val totalChecklistCount: Int
        get() {
            val sectionD = this.sectionD ?: return checklistItems.size
            return checklistItems.size + sectionD.academicRows.size + sectionD.supportRows.size
        }

    val compliantItemCount: Int
        get() = checklistItems.count {
            it.answer == ChecklistAnswer.Available || it.answer == ChecklistAnswer.Yes
        }

    val pendingItemCount: Int
        get() = checklistItems.count { it.answer == ChecklistAnswer.Unanswered }
}

sealed interface SsqSectionFormUiState {
    data object Loading : SsqSectionFormUiState

    data class Success(val data: SsqSectionFormData) : SsqSectionFormUiState

    data class Error(val message: String) : SsqSectionFormUiState

    companion object {
        /** SSQ A1.4 Programme Assessed options. */
        private val programmeOptions = listOf(
            "BCHS",
            "CHO",
            "CHEW / HND",
            "JCHEW / ND",
        )

        fun programmeOptions(): List<String> = programmeOptions

        /** SSQ A1.8 Current Accreditation Status. */
        fun accreditationStatusOptions(): List<String> = listOf(
            "Full",
            "Provisional",
            "Denial",
            "Suspension",
            "Others Specify",
        )

        fun employmentStatusOptions(): List<String> = listOf("Full-Time", "Part-Time")

        fun sexOptions(): List<String> = listOf("Male", "Female")

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
                    SsqSectionKey.C -> sectionC(id, attachments)
                    SsqSectionKey.D -> sectionD(id, attachments)
                    SsqSectionKey.E -> sectionE(id, attachments)
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
                        sex = "Female",
                        licenseNo = "CHPRBN/2012/0488",
                        phone = "+234 803 441 2980",
                        qualification = "M.Sc. PH, CHO",
                        employmentStatus = "Full-Time",
                        roleTitle = "Senior Primary Health Care Tutor",
                    ),
                    PhcTutorUiState(
                        id = "tutor-2",
                        name = "Mr. Emmanuel Chukwu",
                        sex = "Male",
                        licenseNo = "CHPRBN/2016/1129",
                        phone = "+234 802 879 5041",
                        qualification = "B.Sc. Comm Health",
                        employmentStatus = "Full-Time",
                        roleTitle = "Clinical Instructor / Tutor I",
                    ),
                    PhcTutorUiState(
                        id = "tutor-3",
                        name = "Mrs. Fatima Bello Yakubu",
                        sex = "Female",
                        licenseNo = "CHPRBN/2019/3301",
                        phone = "+234 814 309 6712",
                        qualification = "PGD Epidem., CHO",
                        employmentStatus = "Part-Time",
                        roleTitle = "Adjunct Community Field Supervisor",
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

        private fun sectionC(
            id: String,
            attachments: List<SsqAttachmentUiState>,
        ): SsqSectionFormData {
            // Authoritative item text from docs/accrd/SSQ.pdf — Section C1.
            val methodTexts = listOf(
                "Competency-Based Medical Education (CBME)",
                "Problem-Based Learning (PBL)",
                "Team-Based Learning (TBL)",
                "Simulation-based training",
                "Objective Structured Clinical Examination (OSCE)",
                "Skills laboratory teaching",
                "E-learning platform/LMS",
                "Hybrid teaching system",
                "Virtual classroom technology",
                "Smart boards and interactive panels",
                "Virtual reality (VR) simulation tools",
                "Augmented reality (AR) learning tools",
                "Digital anatomy/physiology software",
                "Electronic assessment systems",
                "Computer-Based Testing (CBT) facilities",
                "Learning analytics systems",
                "Access to online journals/databases",
                "Open Educational Resources (OER) integration",
            )
            // HTML stitch groups Units 1–6 / 7–12 / 13–18 as three clusters.
            val clusters = listOf(
                SsqChecklistSubsectionUiState(
                    id = "C1-core",
                    badge = "C1.1",
                    title = "Cluster 1: Core Pedagogy Methods",
                    subtitle = "Units 1–6",
                    tabLabel = "Core Methods",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
                SsqChecklistSubsectionUiState(
                    id = "C1-digital",
                    badge = "C1.2",
                    title = "Cluster 2: Digital Learning Technology",
                    subtitle = "Units 7–12",
                    tabLabel = "Digital Tech",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
                SsqChecklistSubsectionUiState(
                    id = "C1-assessment",
                    badge = "C1.3",
                    title = "Cluster 3: Assessment & Learning Resources",
                    subtitle = "Units 13–18",
                    tabLabel = "Assessment",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
            )
            val clusterIds = listOf("C1-core", "C1-digital", "C1-assessment")
            // Placeholder: 8 of 18 evaluated (matches stitch progress sample).
            val answeredThrough = 8
            val items = methodTexts.mapIndexed { index, text ->
                val clusterId = clusterIds[index / 6]
                val answered = index < answeredThrough
                SsqChecklistItemUiState(
                    id = "C1-${index + 1}",
                    text = text,
                    answer = if (answered) ChecklistAnswer.Available else ChecklistAnswer.Unanswered,
                    remarks = when (index) {
                        0 -> "CBME framework mapped to CHPRBN competency matrix in syllabus."
                        3 -> "Simulation lab timetable and manikin inventory verified."
                        6 -> "LMS licenses active for current academic session."
                        else -> ""
                    },
                    usesAvailability = true,
                    subsectionId = clusterId,
                    numberInSubsection = index + 1,
                )
            }
            return SsqSectionFormData(
                institutionId = id,
                sectionKey = SsqSectionKey.C,
                title = "Curriculum & Teaching Methods",
                subtitle =
                    "Pedagogical compliance audit covering 18 approved teaching methods, lesson guides, and clinical logbooks.",
                formCodeLabel = "CHPRBN STANDARDS SSQ C1",
                status = SectionDraftStatus.InProgress,
                checklistItems = items,
                checklistSubsections = clusters,
                attachments = listOf(
                    SsqAttachmentUiState(
                        id = "c-att-1",
                        title = "Curriculum_Syllabus_2024.pdf",
                        sizeLabel = "4.2 MB",
                        isPhoto = false,
                    ),
                    SsqAttachmentUiState(
                        id = "c-att-2",
                        title = "Lecture_Timetable_Semester1.pdf",
                        sizeLabel = "1.8 MB",
                        isPhoto = false,
                    ),
                    SsqAttachmentUiState(
                        id = "c-att-3",
                        title = "Community_Logbook_Sample.jpg",
                        sizeLabel = "2.1 MB",
                        isPhoto = true,
                    ),
                ),
            )
        }

        private fun sectionD(
            id: String,
            attachments: List<SsqAttachmentUiState>,
        ): SsqSectionFormData {
            // Authoritative ranks/categories from docs/accrd/SSQ.pdf — Section D.
            val academicRanks = listOf(
                "Professor",
                "Associate Professor/Reader",
                "Senior Lecturer",
                "Lecturer I",
                "Lecturer II",
                "Assistant Lecturer",
                "Clinical Instructors",
                "Adjunct/Visiting Lecturers",
            )
            val supportCategories = listOf(
                "Administrative Officers",
                "ICT Officers",
                "Laboratory Scientists/Technologists",
                "Library Officers",
                "Simulation Technicians",
                "Clinical Skills Assistants",
                "Data Managers",
                "Secretarial Staff",
                "Support Staff",
            )
            val d2Texts = listOf(
                "Staff professionally registered with CHPRBN",
                "Staff with valid license",
                "Staff-to-student ratio compliant with NUC/CHPRBN standards",
                "Academic staff possess postgraduate qualifications",
                "Clinical instructors available",
                "ICT competence among staff",
                "Staff trained in simulation-based education",
                "Continuous Professional Development (CPD) evidence available",
                "Research publications evidence available",
                "Staff mentorship programme available",
                "Gender balance in staffing considered",
                "International collaborations/exchanges available",
            )
            val academicSample = listOf(
                Triple("2", "2", "0"),
                Triple("3", "3", "0"),
                Triple("6", "5", "1"),
                Triple("8", "7", "1"),
                Triple("6", "5", "1"),
                Triple("4", "3", "1"),
                Triple("6", "5", "1"),
                Triple("4", "2", "2"),
            )
            val supportSample = listOf("2", "3", "5", "2", "2", "3", "2", "4", "3")
            val sectionD = SectionDFormUiState(
                academicRows = academicRanks.mapIndexed { index, label ->
                    val sample = academicSample.getOrNull(index)
                    StaffingRowUiState(
                        id = "d1-${index + 1}",
                        label = label,
                        required = sample?.first.orEmpty(),
                        available = sample?.let { (it.second.toInt() + it.third.toInt()).toString() }.orEmpty(),
                        fullTime = sample?.second.orEmpty(),
                        partTime = sample?.third.orEmpty(),
                        includesEmploymentSplit = true,
                    )
                },
                supportRows = supportCategories.mapIndexed { index, label ->
                    StaffingRowUiState(
                        id = "d3-${index + 1}",
                        label = label,
                        required = supportSample.getOrNull(index).orEmpty(),
                        available = supportSample.getOrNull(index).orEmpty(),
                        includesEmploymentSplit = false,
                    )
                },
            )
            val answeredThrough = 6
            val d2Items = d2Texts.mapIndexed { index, text ->
                SsqChecklistItemUiState(
                    id = "D2-${index + 1}",
                    text = text,
                    answer = if (index < answeredThrough) ChecklistAnswer.Yes else ChecklistAnswer.Unanswered,
                    remarks = when (index) {
                        0 -> "Audited licenses against CHPRBN registry; certificates current."
                        1 -> "All sampled staff hold valid practicing licenses."
                        7 -> "CPD workshop attendance logs verified for 2023/2024."
                        else -> ""
                    },
                    usesAvailability = false,
                    subsectionId = "D2",
                    numberInSubsection = index + 1,
                )
            }
            return SsqSectionFormData(
                institutionId = id,
                sectionKey = SsqSectionKey.D,
                title = "Staffing & Academic Administration",
                subtitle =
                    "Regulatory evaluation of academic cadre distribution, qualification compliance, and administrative support personnel.",
                formCodeLabel = "CHPRBN Standards SSQ D1–D3",
                status = SectionDraftStatus.InProgress,
                sectionD = sectionD,
                checklistItems = d2Items,
                checklistSubsections = listOf(
                    SsqChecklistSubsectionUiState(
                        id = "D1",
                        badge = "D1",
                        title = "D1: Academic Teaching Cadre",
                        subtitle = "Required / available by rank",
                        tabLabel = "D1: Cadre",
                        attachmentsTitle = "",
                        attachmentsHint = "",
                    ),
                    SsqChecklistSubsectionUiState(
                        id = "D2",
                        badge = "D2",
                        title = "D2: Staff Qualifications & Competencies",
                        subtitle = "12 Yes/No statutory standards",
                        tabLabel = "D2: Standards",
                        attachmentsTitle = "",
                        attachmentsHint = "",
                    ),
                    SsqChecklistSubsectionUiState(
                        id = "D3",
                        badge = "D3",
                        title = "D3: Administrative & Technical Staff",
                        subtitle = "Support cadre inventory",
                        tabLabel = "D3: Support",
                        attachmentsTitle = "",
                        attachmentsHint = "",
                    ),
                ),
                attachments = listOf(
                    SsqAttachmentUiState(
                        id = "d-att-1",
                        title = "Academic_Staff_Roster_2024.pdf",
                        sizeLabel = "3.1 MB",
                        isPhoto = false,
                    ),
                    SsqAttachmentUiState(
                        id = "d-att-2",
                        title = "CHPRBN_License_Batch.pdf",
                        sizeLabel = "2.4 MB",
                        isPhoto = false,
                    ),
                ),
            )
        }

        private fun sectionE(
            id: String,
            attachments: List<SsqAttachmentUiState>,
        ): SsqSectionFormData {
            // Authoritative facility text from docs/accrd/SSQ.pdf — Section E.
            val e1Texts = listOf(
                "Lecture theatres",
                "Smart classrooms",
                "Audio-visual systems",
                "Interactive whiteboards",
                "Public address system",
                "Internet connectivity",
                "Backup power supply",
                "Ergonomic seating",
                "Accessibility for persons with disabilities",
                "CCTV and security systems",
            )
            val e2Texts = listOf(
                "Community Health Skill Laboratory",
                "SOP and other treatment protocol (CHO National Standing Orders)",
                "Anatomy laboratory",
                "Physiology laboratory",
                "Microbiology laboratory",
                "Chemistry laboratory",
                "Nutrition laboratory",
                "ICT laboratory",
                "Simulation laboratory",
                "Telemedicine laboratory",
                "Public health emergency response simulation unit",
                "Maternal and child health demonstration unit",
                "Disease surveillance and GIS unit",
                "Digital diagnostics demonstration unit",
                "AI-enabled health training systems",
            )
            val e3Texts = listOf(
                "High-fidelity patient simulators",
                "Low-fidelity mannequins",
                "Maternal delivery simulators",
                "Neonatal resuscitation simulators",
                "Emergency response simulators",
                "VR clinical simulation systems",
                "AR anatomy teaching systems",
                "Digital stethoscopes",
                "Electronic medical record training software",
                "Mobile health (mHealth) demonstration tools",
                "Drone-assisted healthcare demonstration technology",
                "AI-supported diagnostic training software",
                "Digital microscopy systems",
                "Smart attendance and biometric systems",
                "Learning management systems (LMS)",
            )
            val subsections = listOf(
                SsqChecklistSubsectionUiState(
                    id = "E1",
                    badge = "E1",
                    title = "E1: Lecture & Classroom Facilities",
                    subtitle = "10 lecture facilities",
                    tabLabel = "E1: Lecture",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
                SsqChecklistSubsectionUiState(
                    id = "E2",
                    badge = "E2",
                    title = "E2: Laboratories and Skills Facilities",
                    subtitle = "15 labs & skills units",
                    tabLabel = "E2: Labs",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
                SsqChecklistSubsectionUiState(
                    id = "E3",
                    badge = "E3",
                    title = "E3: Simulation & Modern Technology",
                    subtitle = "15 modern facilities",
                    tabLabel = "E3: Sim",
                    attachmentsTitle = "",
                    attachmentsHint = "",
                ),
            )
            fun itemsFor(
                texts: List<String>,
                subsectionId: String,
                answeredThrough: Int,
            ): List<SsqChecklistItemUiState> = texts.mapIndexed { index, text ->
                SsqChecklistItemUiState(
                    id = "$subsectionId-${index + 1}",
                    text = text,
                    answer = if (index < answeredThrough) {
                        ChecklistAnswer.Available
                    } else {
                        ChecklistAnswer.Unanswered
                    },
                    remarks = when {
                        subsectionId == "E1" && index == 0 ->
                            "Main theatre seats 180; dual exit doors verified."
                        subsectionId == "E1" && index == 2 ->
                            "Four projection rigs with HDMI sync active."
                        subsectionId == "E2" && index == 0 ->
                            "Skill lab inventory and SOP binder reviewed."
                        else -> ""
                    },
                    usesAvailability = true,
                    subsectionId = subsectionId,
                    numberInSubsection = index + 1,
                )
            }
            // Sample progress: 4 of 10 E1 answered (matches partial inspection).
            val items = itemsFor(e1Texts, "E1", answeredThrough = 4) +
                itemsFor(e2Texts, "E2", answeredThrough = 0) +
                itemsFor(e3Texts, "E3", answeredThrough = 0)
            return SsqSectionFormData(
                institutionId = id,
                sectionKey = SsqSectionKey.E,
                title = "Physical Facilities, Labs & Technology",
                subtitle =
                    "Verify institutional learning environments, lab equipment, and simulation capacity per CHPRBN regulatory minimums.",
                formCodeLabel = "CHPRBN Standards SSQ E1–E3",
                status = SectionDraftStatus.InProgress,
                checklistItems = items,
                checklistSubsections = subsections,
                attachments = listOf(
                    SsqAttachmentUiState(
                        id = "e-att-1",
                        title = "Lecture_Hall_A.jpg",
                        sizeLabel = "3.8 MB",
                        isPhoto = true,
                    ),
                    SsqAttachmentUiState(
                        id = "e-att-2",
                        title = "Microbiology_Lab.jpg",
                        sizeLabel = "2.9 MB",
                        isPhoto = true,
                    ),
                    SsqAttachmentUiState(
                        id = "e-att-3",
                        title = "MCH_Simulation.jpg",
                        sizeLabel = "3.1 MB",
                        isPhoto = true,
                    ),
                ),
            )
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
