package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * The dashboard always loads asynchronously from cache + session lookup,
 * so a dedicated `Loading` arm is convenient even though the rest of the
 * domain prefers `null`/empty defaults.
 */
sealed interface ExamDashboardResult {
    data object Loading : ExamDashboardResult
    data class Success(val summary: ExamDashboardSummary) : ExamDashboardResult
    data class Error(val message: String) : ExamDashboardResult
}
