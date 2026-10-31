package ng.com.chprbn.mobile.feature.accreditation.presentation

/**
 * Server-returned accreditation grade. Display-only — never computed on device.
 */
enum class AccreditationServerGrade {
    Excellent,
    Satisfactory,
    ReVisit,
    Denial,
}

/**
 * Server-returned accreditation decision. Display-only.
 */
enum class AccreditationServerDecision {
    FullAccreditation,
    ProvisionalConditional,
    Denial,
    Revocation,
    Deferral,
}

data class AssessmentResultScoreRow(
    val sectionIndex: Int,
    val groupLabel: String,
    val rawScore: String,
    val maxPoints: Int,
)

data class AssessmentResultData(
    val institutionId: String,
    val decisionId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val assessmentDateLabel: String,
    val grade: AccreditationServerGrade,
    val decision: AccreditationServerDecision,
    val accreditationPeriod: String,
    val overallScorePercentLabel: String,
    val studentQuotaLabel: String,
    val receiptRef: String,
    val rawScoreRows: List<AssessmentResultScoreRow>,
    val conditions: List<String>,
    val showCertificateDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
)

sealed interface AssessmentResultUiState {
    data object Loading : AssessmentResultUiState

    data class Success(val data: AssessmentResultData) : AssessmentResultUiState

    data class Error(val message: String) : AssessmentResultUiState

    companion object {
        fun placeholder(
            institutionId: String = "inst-abuth",
            decisionId: String = "dec-inst-abuth-2024",
        ): Success = Success(
            AssessmentResultData(
                institutionId = institutionId.ifBlank { "inst-abuth" },
                decisionId = decisionId.ifBlank { "dec-${institutionId.ifBlank { "inst-abuth" }}-2024" },
                institutionName = "Ahmadu Bello University Teaching Hospital",
                locationLabel = "Zaria, Kaduna State",
                facilityCode = "CHPRBN-NW-2024-08",
                assessmentDateLabel = "24 Oct 2024",
                grade = AccreditationServerGrade.Excellent,
                decision = AccreditationServerDecision.FullAccreditation,
                accreditationPeriod = "5 Years Full Accreditation",
                overallScorePercentLabel = "88.0%",
                studentQuotaLabel = "120",
                receiptRef = "REC-2024-ABU-8829-SYNC",
                rawScoreRows = listOf(
                    AssessmentResultScoreRow(1, "Group 1: Location & Land Area (§1)", "9", 10),
                    AssessmentResultScoreRow(2, "Group 2: Head of Department (§2)", "10", 10),
                    AssessmentResultScoreRow(3, "Group 3: Human Resources & Teaching Cadre (§3)", "22", 25),
                    AssessmentResultScoreRow(4, "Group 4: Practice Area & Clinical Postings (§5)", "14", 15),
                    AssessmentResultScoreRow(5, "Group 5: Physical Facilities & Labs (§6)", "21", 25),
                    AssessmentResultScoreRow(6, "Group 6: Capital Base & Welfare (§7 & §8)", "13", 15),
                ),
                conditions = listOf(
                    "Maintain approved teacher-to-student clinical supervision ratio (1:8).",
                    "Renew affiliated health facility tripartite MoUs 6 months prior to expiry in Oct 2026.",
                    "Submit annual faculty continuing education (MCPDP) audit logbook.",
                ),
            ),
        )
    }
}
