package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * One of the practical sections under a schedule (e.g. "A — Patient
 * Assessment", "B — Clinical Diagnosis"). Read-only reference data
 * downloaded as part of the schedule package; never mutated locally.
 */
data class PracticalSection(
    val id: String,
    val scheduleId: String,
    val title: String,
    val subtitle: String,
    val ordering: Int,
)
