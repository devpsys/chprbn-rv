package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Outcome of pulling a schedule's reference package (paper + sections +
 * questions + candidate roster) into the local cache. Carries counters on
 * success so the Download Loading screen can surface meaningful progress
 * once the operation completes.
 */
sealed interface DownloadAssessmentPackageResult {
    data class Success(
        val scheduleId: String,
        val candidatesCount: Int,
        val sectionsCount: Int,
        val questionsCount: Int,
    ) : DownloadAssessmentPackageResult

    data class Error(val message: String) : DownloadAssessmentPackageResult
}
