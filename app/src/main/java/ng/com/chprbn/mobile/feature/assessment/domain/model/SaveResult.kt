package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Generic command result for any save / mutate operation in the assessment
 * domain (record score, commit section, record project score, clear cache).
 *
 * Deliberately a single shared type rather than per-operation `SaveXyzResult`
 * because every assessment write has the same shape — a binary outcome with
 * an opaque error string. When a specific operation needs richer success
 * data (e.g. the server-assigned ID), a sibling sealed type is fair game,
 * but no operation needs that today.
 */
sealed interface SaveResult {
    data object Success : SaveResult
    data class Error(val message: String) : SaveResult
}
