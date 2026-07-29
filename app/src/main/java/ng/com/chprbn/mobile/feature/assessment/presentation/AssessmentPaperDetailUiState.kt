package ng.com.chprbn.mobile.feature.assessment.presentation

/** Sync state of an individual candidate row in the directory. */
enum class CandidateSyncStatus { Synced, Unsynced }

data class CandidateRowUiState(
    val id: String,
    val initials: String,
    val fullName: String,
    val syncStatus: CandidateSyncStatus,
)

/**
 * Paper Detail screen state. UI-composition only today — every value is
 * hardcoded by the ViewModel against the design's demo content. When a real
 * data source lands the VM will fetch by `scheduleId` and populate this
 * state; the screen contract stays unchanged.
 */
data class AssessmentPaperDetailUiState(
    val paperTitle: String = "",
    val statusLabel: String = "",
    val progressFraction: Float = 0f,
    val checkedInCount: Int = 0,
    val totalCount: Int = 0,
    val facilityName: String = "",
    val facilityAddress: String = "",
    val hallName: String = "",
    val hallAddress: String = "",
    val candidates: List<CandidateRowUiState> = emptyList(),
    val heroImageUrl: String? = null,
    /**
     * Non-null when the paper fetch surfaced an error (either NotFound or a
     * repository-side error). The screen renders a small banner so the
     * officer knows the empty state isn't just "nothing yet" (A-S7 audit
     * fix). Downloading the package or retrying clears it.
     */
    val errorMessage: String? = null,
)
