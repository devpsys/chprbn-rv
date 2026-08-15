package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamCandidateUiState(
    /** Real DB candidate id — used for the Add Remark dialog and profile nav, never shown in the UI (see [idLabel]). */
    val candidateId: String = "",
    /**
     * Candidate photo. `null` means "no photo available" — the card renders
     * a bundled `Icons.Filled.AccountCircle` inside a green-tinted circle
     * (E11 audit fix — the pre-fix code fell back to hotlinked
     * `lh3.googleusercontent.com` stock photos that could 404).
     */
    val avatarUrl: String?,
    val name: String,
    val idLabel: String,
    /**
     * Attendance status text used on the top-right pill: "Signed In",
     * "Signed Out", "Flagged", or "Pending". Remark count is a separate
     * signal ([remarkCount]) so the pill stays a pure attendance signal
     * and the "N Remark" state renders on the Add-Remark button.
     */
    val statusPillLabel: String,
    val statusSubLabel: String,
    /**
     * Number of remarks the officer has logged for this candidate. When
     * `> 0`, the "Add Remark" button in the card renders as a warning-
     * coloured "N Remark" indicator (design spec).
     */
    val remarkCount: Int = 0,
)

data class ExamCandidatesUiState(
    val searchQuery: String,
    val activeFilterLabel: String,
    val filterLabels: List<String>,
    val candidates: List<ExamCandidateUiState>,
    /** True until the first [ExamCandidatesViewModel.refresh] completes — gates the loading spinner. */
    val hasLoaded: Boolean = false,
) {
    companion object {
        private val DEFAULT_FILTER_LABELS = listOf("All", "Signed In", "Signed Out", "Flagged")

        /**
         * Real starting state — no candidates, [hasLoaded] false so
         * [ExamCandidatesContent] renders the loading spinner until
         * [ExamCandidatesViewModel.refresh] resolves. The ViewModel never
         * shows [preview]'s made-up names to a real officer, not even
         * momentarily.
         */
        fun initial(): ExamCandidatesUiState = ExamCandidatesUiState(
            hasLoaded = false,
            searchQuery = "",
            activeFilterLabel = "All",
            filterLabels = DEFAULT_FILTER_LABELS,
            candidates = emptyList(),
        )

        /**
         * Sample data for the Compose `@Preview` and content-render tests —
         * never wired into the real [ExamCandidatesViewModel] flow (see
         * [initial]). Uses null avatars so the card renders the bundled
         * Icon fallback rather than a hotlinked stock photo.
         */
        fun preview(): ExamCandidatesUiState = ExamCandidatesUiState(
            hasLoaded = true,
            searchQuery = "",
            activeFilterLabel = "All",
            filterLabels = DEFAULT_FILTER_LABELS,
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
                    statusSubLabel = "09:12 AM",
                    remarkCount = 1,
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
