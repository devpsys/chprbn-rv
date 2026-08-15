package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Exam centre / institution. Read-only reference data downloaded as part
 * of the day's dossier. Same `id` is used by [Paper.centerId] and
 * [OfficerSession.centerId].
 */
data class Center(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val heroImageUrl: String? = null,
    /** True when the dossier's `data.sections` array was non-empty. Drives the Practical Assessment card on the dashboard. */
    val hasSections: Boolean = false,
    /** The dossier's `data.year` — required verbatim on every `attendance/push-record` row (`docs/mobile-api-guide.html` §5). */
    val year: Int? = null,
)
