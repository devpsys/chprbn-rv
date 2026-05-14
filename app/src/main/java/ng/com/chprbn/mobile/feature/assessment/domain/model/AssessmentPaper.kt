package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Detail shown on `AssessmentPaperDetailScreen`. Facility and hall are
 * tightly bound to the paper (one paper = one venue) so they live in the
 * same file rather than as standalone aggregates.
 */
data class AssessmentPaper(
    val scheduleId: String,
    val title: String,
    val statusLabel: String,
    val facility: Facility,
    val hall: Hall,
    val heroImageUrl: String? = null,
)

data class Facility(val name: String, val address: String)

data class Hall(val name: String, val address: String)
