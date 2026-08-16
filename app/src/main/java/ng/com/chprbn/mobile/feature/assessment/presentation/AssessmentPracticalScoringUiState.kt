package ng.com.chprbn.mobile.feature.assessment.presentation

data class ScoreQuestionUiState(
    val id: String,
    /** 1-based question number used for the prompt prefix and a11y labels. */
    val number: Int,
    val prompt: String,
    val imageUrl: String?,
    val maxScore: Int,
    val score: Int,
) {
    /** Treats any non-zero score as scored. Placeholder rule until real
     *  state lands — a question may legitimately be scored zero, in which
     *  case the VM will need to carry an explicit `isScored` flag. */
    val isScored: Boolean get() = score > 0
}

data class AssessmentPracticalScoringUiState(
    /** Section heading rendered above the question list — the downloaded
     *  section name (e.g. "CHEW - Patient Assessment"). The screen
     *  uppercases for display. */
    val sectionTitle: String = "",
    val questions: List<ScoreQuestionUiState> = emptyList(),
    /** True while [AssessmentPracticalScoringViewModel.onSaveScores] is
     *  committing the section; the Save FAB shows a spinner and ignores
     *  further taps. */
    val isSaving: Boolean = false,
)
