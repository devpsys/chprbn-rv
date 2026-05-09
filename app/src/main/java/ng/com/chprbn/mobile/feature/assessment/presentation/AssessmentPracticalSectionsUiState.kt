package ng.com.chprbn.mobile.feature.assessment.presentation

/**
 * Lifecycle status of a single practical-assessment section. The screen
 * derives icon, status-pill colour, and footer copy from this enum.
 */
enum class PracticalSectionStatus { Complete, Incomplete, NotStarted }

data class PracticalSectionUiState(
    val id: String,
    /** Short label (e.g. "Section A"); rendered uppercase. */
    val sectionTitle: String,
    /** Subtitle below the section title (e.g. "Patient Assessment"). */
    val sectionSubtitle: String,
    val status: PracticalSectionStatus,
    /**
     * Status-specific footer text. For Complete: "Updated: 09:45 AM";
     * Incomplete: "2 tasks remaining"; NotStarted: "No data recorded".
     * The screen formats these via strings.xml; this carries the
     * already-formatted display string today.
     */
    val footerText: String,
)

data class AssessmentPracticalSectionsUiState(
    val candidateName: String = "",
    val candidateExamId: String = "",
    val candidatePhotoUrl: String? = null,
    val sectionsDone: Int = 0,
    val sectionsTotal: Int = 0,
    val sectionsRemaining: Int = 0,
    val sections: List<PracticalSectionUiState> = emptyList(),
)
