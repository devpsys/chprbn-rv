package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Result of fetching the detail for a single schedule's paper. Matches the
 * verification feature's 3-arm sealed pattern — `NotFound` is its own arm so
 * the UI can show a deliberate empty state without treating it as failure.
 */
sealed interface AssessmentPaperDetailResult {
    data class Success(val paper: AssessmentPaper) : AssessmentPaperDetailResult
    data object NotFound : AssessmentPaperDetailResult
    data class Error(val message: String) : AssessmentPaperDetailResult
}
