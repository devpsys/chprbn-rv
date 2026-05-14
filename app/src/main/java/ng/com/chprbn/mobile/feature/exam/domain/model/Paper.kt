package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.PaperKind

/**
 * One exam paper on the day's schedule. [startAt]/[endAt] are epoch
 * millis. [totalCandidates] is the size of the assignment list — the
 * actual checked-in count comes from `AttendanceRepository`.
 */
data class Paper(
    val id: String,
    val centerId: String,
    val title: String,
    val subtitle: String,
    val paperKind: PaperKind,
    val startAt: Long,
    val endAt: Long,
    val hall: String,
    val totalCandidates: Int,
)
