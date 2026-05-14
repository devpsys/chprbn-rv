package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Generic command result for exam-side save / mutate operations that
 * don't return richer success data (clear cache, etc.). Matches the
 * assessment-side `SaveResult` shape verbatim; the duplication is
 * deliberate — feature-scoped types keep imports unambiguous and
 * decouple a future shape change in one feature from the other.
 */
sealed interface SaveResult {
    data object Success : SaveResult
    data class Error(val message: String) : SaveResult
}
