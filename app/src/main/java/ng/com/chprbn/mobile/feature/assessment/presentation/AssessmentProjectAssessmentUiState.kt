package ng.com.chprbn.mobile.feature.assessment.presentation

data class AssessmentProjectAssessmentUiState(
    val candidateName: String = "",
    /** Exam id rendered next to the candidate name (e.g. "EX-2024-0092"). */
    val examId: String = "",
    /** Practitioner role / category (e.g. "Clinical Practitioner"). */
    val role: String = "",
    val photoUrl: String? = null,
    /** Drives the small primary verified badge over the avatar's bottom-right. */
    val verified: Boolean = false,
    /** Current score input text. Kept as String so partial entries like
     *  "8." or "" can be represented without losing user state. */
    val scoreText: String = "",
    val maxScore: Int = 10,
)
