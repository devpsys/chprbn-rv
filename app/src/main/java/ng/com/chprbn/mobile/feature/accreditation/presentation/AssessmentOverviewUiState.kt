package ng.com.chprbn.mobile.feature.accreditation.presentation

data class SsqSectionSummaryUiState(
    val key: SsqSectionKey,
    val codeLabel: String,
    val rangeLabel: String,
    val title: String,
    val subtitle: String,
    val status: SectionDraftStatus,
    val itemsCompleted: Int,
    val totalItems: Int,
)

data class AssessmentOverviewData(
    val institutionId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val cycleLabel: String,
    val sections: List<SsqSectionSummaryUiState>,
) {
    val savedCount: Int
        get() = sections.count { it.status.isSavedLocallyOrBeyond() }

    val progressFraction: Float
        get() = if (sections.isEmpty()) {
            0f
        } else {
            savedCount.toFloat() / sections.size.toFloat()
        }

    val allSectionsSaved: Boolean
        get() = sections.isNotEmpty() && sections.all { it.status.isSavedLocallyOrBeyond() }
}

private fun SectionDraftStatus.isSavedLocallyOrBeyond(): Boolean = when (this) {
    SectionDraftStatus.SavedLocally,
    SectionDraftStatus.PendingSync,
    SectionDraftStatus.Synced,
    -> true
    SectionDraftStatus.NotStarted,
    SectionDraftStatus.InProgress,
    -> false
}

sealed interface AssessmentOverviewUiState {
    data object Loading : AssessmentOverviewUiState

    data class Success(val data: AssessmentOverviewData) : AssessmentOverviewUiState

    data class Error(val message: String) : AssessmentOverviewUiState

    companion object {
        /**
         * Placeholder matching Prompt C overview HTML — 3/6 saved (50%).
         * Replaced once a GetAssessmentOverview use case exists.
         */
        fun placeholder(institutionId: String = "inst-abuth"): Success = Success(
            AssessmentOverviewData(
                institutionId = institutionId.ifBlank { "inst-abuth" },
                institutionName = "Ahmadu Bello University Teaching Hospital",
                locationLabel = "Zaria, Kaduna State",
                facilityCode = "ABUTH-ZR-049",
                cycleLabel = "Cycle 2024/2025",
                sections = listOf(
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.A,
                        codeLabel = "Section A",
                        rangeLabel = "SSQ A1 + A2",
                        title = "General Information",
                        subtitle = "24 mandatory baseline fields captured",
                        status = SectionDraftStatus.SavedLocally,
                        itemsCompleted = 24,
                        totalItems = 24,
                    ),
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.B,
                        codeLabel = "Section B",
                        rangeLabel = "B1 + B2",
                        title = "Philosophy & Governance",
                        subtitle = "B1 philosophy/objectives + B2 governance (20 items)",
                        status = SectionDraftStatus.InProgress,
                        itemsCompleted = 3,
                        totalItems = 20,
                    ),
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.C,
                        codeLabel = "Section C",
                        rangeLabel = "C1",
                        title = "Curriculum & Teaching Methods",
                        subtitle = "8 of 14 learning modules annotated",
                        status = SectionDraftStatus.InProgress,
                        itemsCompleted = 8,
                        totalItems = 18,
                    ),
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.D,
                        codeLabel = "Section D",
                        rangeLabel = "D1 - D3",
                        title = "Staffing & Academic Administration",
                        subtitle = "Faculty credentials & license registry attached",
                        status = SectionDraftStatus.SavedLocally,
                        itemsCompleted = 12,
                        totalItems = 12,
                    ),
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.E,
                        codeLabel = "Section E",
                        rangeLabel = "E1 - E3",
                        title = "Physical Facilities, Labs & Technology",
                        subtitle = "Equipment, simulation centre & IT inventory",
                        status = SectionDraftStatus.NotStarted,
                        itemsCompleted = 0,
                        totalItems = 40,
                    ),
                    SsqSectionSummaryUiState(
                        key = SsqSectionKey.F,
                        codeLabel = "Section F",
                        rangeLabel = "F1",
                        title = "Affiliated Clinical Facilities & Sites",
                        subtitle = "Community postings & MoU verification",
                        status = SectionDraftStatus.NotStarted,
                        itemsCompleted = 0,
                        totalItems = 7,
                    ),
                ),
            ),
        )
    }
}
