package ng.com.chprbn.mobile.feature.exam.presentation

import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

data class RemarkRowUiState(
    val id: String,
    val body: String,
    val severity: RemarkSeverity,
    val createdAtLabel: String,
)

data class CandidateProfileUiState(
    val candidateName: String,
    val examNumberLabel: String,
    val photoUrl: String?,
    val remarks: List<RemarkRowUiState>,
    /** "Signed In" / "Signed Out" / "Flagged" / "Pending" — the candidate's attendance for the roster's paper. Same vocabulary as `ExamCandidateUiState.statusPillLabel`. */
    val statusPillLabel: String = "Pending",
    /** True once a real load completes — gates the real empty state vs. the placeholder roster. */
    val hasLoaded: Boolean = false,
    /** True when [hasLoaded] and the candidateId nav arg didn't resolve to a locally-cached candidate. */
    val notFound: Boolean = false,
) {
    companion object {
        fun placeholder(): CandidateProfileUiState = CandidateProfileUiState(
            candidateName = "Marcus Thompson",
            examNumberLabel = "ID: EX-2024-0892",
            photoUrl = null,
            statusPillLabel = "Signed In",
            remarks = listOf(
                RemarkRowUiState(
                    id = "placeholder-1",
                    body = "Absent with Excuse",
                    severity = RemarkSeverity.Warning,
                    createdAtLabel = "Jun 12, 2024 at 9:05 AM",
                ),
            ),
        )
    }
}
