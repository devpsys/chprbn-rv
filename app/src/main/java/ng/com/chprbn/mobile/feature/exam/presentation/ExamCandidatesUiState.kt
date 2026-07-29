package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamCandidateUiState(
    /**
     * Candidate photo. `null` means "no photo available" — the card renders
     * a bundled `Icons.Filled.AccountCircle` inside a green-tinted circle
     * (E11 audit fix — the pre-fix code fell back to hotlinked
     * `lh3.googleusercontent.com` stock photos that could 404).
     */
    val avatarUrl: String?,
    val name: String,
    val idLabel: String,
    val statusPillLabel: String,
    val statusSubLabel: String,
)

data class ExamCandidatesUiState(
    val searchQuery: String,
    val activeFilterLabel: String,
    val filterLabels: List<String>,
    val candidates: List<ExamCandidateUiState>
) {
    companion object {
        fun placeholder(): ExamCandidatesUiState = ExamCandidatesUiState(
            searchQuery = "",
            activeFilterLabel = "All",
            filterLabels = listOf("All", "Signed In", "Signed Out", "Flagged"),
            // Placeholder candidates use null avatars so the card renders the
            // bundled Icon fallback — never a hotlinked stock photo.
            candidates = listOf(
                ExamCandidateUiState(
                    avatarUrl = null,
                    name = "Marcus Thompson",
                    idLabel = "ID: EX-2024-0892",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "08:45 AM"
                ),
                ExamCandidateUiState(
                    avatarUrl = null,
                    name = "Sarah Jenkins",
                    idLabel = "ID: EX-2024-1023",
                    statusPillLabel = "Signed Out",
                    statusSubLabel = "Pending"
                ),
                ExamCandidateUiState(
                    avatarUrl = null,
                    name = "David Chen",
                    idLabel = "ID: EX-2024-0741",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "09:12 AM"
                ),
                ExamCandidateUiState(
                    avatarUrl = null,
                    name = "Elena Rodriguez",
                    idLabel = "ID: EX-2024-0556",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "08:58 AM"
                )
            )
        )
    }
}
