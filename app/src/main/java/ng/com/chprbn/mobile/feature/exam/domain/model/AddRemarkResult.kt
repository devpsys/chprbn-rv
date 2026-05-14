package ng.com.chprbn.mobile.feature.exam.domain.model

sealed interface AddRemarkResult {
    data class Success(val remark: Remark) : AddRemarkResult
    data class Error(val message: String) : AddRemarkResult
}
