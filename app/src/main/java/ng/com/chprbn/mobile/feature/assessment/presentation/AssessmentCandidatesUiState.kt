package ng.com.chprbn.mobile.feature.assessment.presentation

/** Renders the candidates as either a vertical list or a 2-column grid. */
enum class CandidatesViewMode { List, Grid }

/**
 * Drives the score-pill color band. The threshold (`score < 50`) is applied
 * by the VM so the screen layer doesn't carry business logic; the screen
 * just reads `level` and picks a colour.
 */
enum class ScoreLevel { Normal, Low }

data class CandidateCardUiState(
    val id: String,
    val indexingNumber: String,
    val fullName: String,
    val photoUrl: String?,
    val score: Int,
    val level: ScoreLevel,
)

data class AssessmentCandidatesUiState(
    val totalCount: Int = 0,
    val query: String = "",
    val viewMode: CandidatesViewMode = CandidatesViewMode.List,
    val candidates: List<CandidateCardUiState> = emptyList(),
)
