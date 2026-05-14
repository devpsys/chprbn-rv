package ng.com.chprbn.mobile.feature.exam.domain.model

/** Three-arm result matching the assessment-side pattern. */
sealed interface ExamPaperDetailResult {
    data class Success(val detail: ExamPaperDetail) : ExamPaperDetailResult
    data object NotFound : ExamPaperDetailResult
    data class Error(val message: String) : ExamPaperDetailResult
}
