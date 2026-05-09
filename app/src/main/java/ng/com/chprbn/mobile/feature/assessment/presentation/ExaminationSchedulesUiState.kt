package ng.com.chprbn.mobile.feature.assessment.presentation

/** Sync state of a single scheduled assessment, drives the status pill colour + label. */
enum class ScheduleSyncStatus { Synced, Pending }

/**
 * One row in the examination-schedules list. Pure presentation; values come
 * from the ViewModel's hardcoded placeholder data today and will be sourced
 * from a use case once the feature owns real state.
 */
data class ScheduleCardUiState(
    val id: String,
    val title: String,
    val dateLabel: String,
    val syncStatus: ScheduleSyncStatus,
)

data class ExaminationSchedulesUiState(
    val schedules: List<ScheduleCardUiState> = emptyList(),
    /**
     * Background image URL for the "Upcoming Assessments" decorative footer.
     * Mirrors the existing heroImageUrl pattern in `ExamDashboardScreen`. Null
     * renders the footer as a flat primary-container card with no image.
     */
    val decorativeImageUrl: String? = null,
)
