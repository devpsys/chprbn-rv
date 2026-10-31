package ng.com.chprbn.mobile.feature.accreditation.presentation

data class SubmitRawMarkRow(
    val groupLabel: String,
    val clauseLabel: String,
    val rawScore: String,
    val maxPoints: Int,
)

data class SubmitForReviewData(
    val institutionId: String,
    val assessmentId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val cycleLabel: String,
    val sectionsCompleted: Int,
    val sectionsTotal: Int,
    val criteriaEntered: Int,
    val criteriaTotal: Int,
    val attachmentCount: Int,
    val scoreGroupsComplete: Int,
    val scoreGroupsTotal: Int,
    val rawMarks: List<SubmitRawMarkRow>,
    val isSubmitting: Boolean = false,
    val showErrorDialog: Boolean = false,
    val requestResultNavigation: Boolean = false,
    val submittedDecisionId: String = "",
)

sealed interface SubmitForReviewUiState {
    data object Loading : SubmitForReviewUiState

    data class Success(val data: SubmitForReviewData) : SubmitForReviewUiState

    data class Error(val message: String) : SubmitForReviewUiState

    companion object {
        fun placeholder(
            institutionId: String = "inst-abuth",
            assessmentId: String = "assess-inst-abuth",
        ): Success = Success(
            SubmitForReviewData(
                institutionId = institutionId.ifBlank { "inst-abuth" },
                assessmentId = assessmentId.ifBlank { "assess-${institutionId.ifBlank { "inst-abuth" }}" },
                institutionName = "Ahmadu Bello University Teaching Hospital",
                locationLabel = "Zaria, Kaduna State",
                facilityCode = "CHPRBN-NW-2024-08",
                cycleLabel = "2024 / 2025",
                sectionsCompleted = 6,
                sectionsTotal = 6,
                criteriaEntered = 8,
                criteriaTotal = 8,
                attachmentCount = 14,
                scoreGroupsComplete = 6,
                scoreGroupsTotal = 6,
                rawMarks = listOf(
                    SubmitRawMarkRow(
                        groupLabel = "Group 1: Location & Land Area",
                        clauseLabel = "§1",
                        rawScore = "9",
                        maxPoints = 10,
                    ),
                    SubmitRawMarkRow(
                        groupLabel = "Group 2: Head of Department",
                        clauseLabel = "§2",
                        rawScore = "10",
                        maxPoints = 10,
                    ),
                    SubmitRawMarkRow(
                        groupLabel = "Group 3: Human Resources & Teaching Cadre",
                        clauseLabel = "§3",
                        rawScore = "22",
                        maxPoints = 25,
                    ),
                    SubmitRawMarkRow(
                        groupLabel = "Group 4: Practice Area & Clinical Postings",
                        clauseLabel = "§5",
                        rawScore = "14",
                        maxPoints = 15,
                    ),
                    SubmitRawMarkRow(
                        groupLabel = "Group 5: Physical Facilities & Labs",
                        clauseLabel = "§6",
                        rawScore = "21",
                        maxPoints = 25,
                    ),
                    SubmitRawMarkRow(
                        groupLabel = "Group 6: Capital Base & Welfare",
                        clauseLabel = "§7 & §8",
                        rawScore = "13",
                        maxPoints = 15,
                    ),
                ),
            ),
        )
    }
}
