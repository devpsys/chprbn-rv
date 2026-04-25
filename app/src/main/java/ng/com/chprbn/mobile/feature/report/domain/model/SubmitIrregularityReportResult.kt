package ng.com.chprbn.mobile.feature.report.domain.model

sealed interface SubmitIrregularityReportResult {
    data object Success : SubmitIrregularityReportResult
    data class Error(val message: String) : SubmitIrregularityReportResult
}
