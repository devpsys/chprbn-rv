package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * One scoreable question in a practical section. Read-only reference data
 * downloaded as part of the schedule package. [imageUrl] is optional — the
 * design uses it for an illustrative photo above the prompt.
 *
 * [maxScore] is the upper bound for the per-candidate `PracticalScore.score`
 * and is also the stepper's max on the scoring screen.
 */
data class SectionQuestion(
    val id: String,
    val sectionId: String,
    val number: Int,
    val prompt: String,
    val imageUrl: String? = null,
    val maxScore: Int,
)
