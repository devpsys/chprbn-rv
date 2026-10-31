package ng.com.chprbn.mobile.feature.accreditation.presentation

enum class ScoreCriterionGroup {
    Location,
    HeadOfDepartment,
    HumanResources,
    PracticeArea,
    PhysicalFacilities,
    FinancialAndWelfare,
}

data class ScoreEntryRowUiState(
    val group: ScoreCriterionGroup,
    val rawScore: String,
    val observations: String,
    val attachments: List<SsqAttachmentUiState>,
) {
    val maxPoints: Int
        get() = when (group) {
            ScoreCriterionGroup.Location -> 10
            ScoreCriterionGroup.HeadOfDepartment -> 10
            ScoreCriterionGroup.HumanResources -> 25
            ScoreCriterionGroup.PracticeArea -> 15
            ScoreCriterionGroup.PhysicalFacilities -> 25
            ScoreCriterionGroup.FinancialAndWelfare -> 15
        }

    val isCompleted: Boolean
        get() = rawScore.isNotBlank()
}

data class ScoreEntryData(
    val institutionId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val rows: List<ScoreEntryRowUiState>,
    val showSavedDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val requestSubmitNavigation: Boolean = false,
) {
    val totalGroups: Int
        get() = rows.size

    val completedCount: Int
        get() = rows.count { it.isCompleted }

    val progressFraction: Float
        get() = if (totalGroups == 0) 0f else completedCount.toFloat() / totalGroups.toFloat()
}

sealed interface ScoreEntryUiState {
    data object Loading : ScoreEntryUiState

    data class Success(val data: ScoreEntryData) : ScoreEntryUiState

    data class Error(val message: String) : ScoreEntryUiState

    companion object {
        fun placeholder(institutionId: String = "inst-abuth"): Success = Success(
            ScoreEntryData(
                institutionId = institutionId.ifBlank { "inst-abuth" },
                institutionName = "Ahmadu Bello University Teaching Hospital",
                locationLabel = "Zaria, Kaduna State • Tertiary Academic Facility",
                facilityCode = "CHPRBN-NW-2024-08",
                rows = listOf(
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.Location,
                        rawScore = "9",
                        observations = "Campus exceeds 5 hectares requirement (6.2 ha measured).",
                        attachments = listOf(
                            SsqAttachmentUiState("s1", "Land_Inspection_Proof.jpg", "720 KB", true),
                        ),
                    ),
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.HeadOfDepartment,
                        rawScore = "10",
                        observations = "Substantive appointment, Master of Public Health.",
                        attachments = emptyList(),
                    ),
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.HumanResources,
                        rawScore = "22",
                        observations = "Cadre verified against nominal roll and payroll.",
                        attachments = emptyList(),
                    ),
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.PracticeArea,
                        rawScore = "14",
                        observations = "Four affiliated facilities with active MoU.",
                        attachments = emptyList(),
                    ),
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.PhysicalFacilities,
                        rawScore = "21",
                        observations = "Section E inventory cross-checked on site.",
                        attachments = listOf(
                            SsqAttachmentUiState("s2", "Lab_Demonstration.jpg", "640 KB", true),
                        ),
                    ),
                    ScoreEntryRowUiState(
                        group = ScoreCriterionGroup.FinancialAndWelfare,
                        rawScore = "12",
                        observations = "Capital base and salary schedules certified.",
                        attachments = emptyList(),
                    ),
                ),
            ),
        )
    }
}
