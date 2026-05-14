package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Aggregated view of one section's scoring progress for a specific candidate,
 * used by the `AssessmentPracticalSections` hub screen. The [section] field
 * carries the static reference data; the rest is derived per candidate.
 *
 * [lastUpdatedAt] is the most recent `scoredAt` among the section's scored
 * questions, or `null` when no question has been scored yet.
 */
data class PracticalSectionSummary(
    val section: PracticalSection,
    val status: PracticalSectionStatus,
    val scoredCount: Int,
    val totalCount: Int,
    val lastUpdatedAt: Long?,
)
