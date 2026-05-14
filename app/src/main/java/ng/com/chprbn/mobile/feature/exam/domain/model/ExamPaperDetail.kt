package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * One-paper detail view backing `ExamPaperScreen`. Joins the paper's
 * static reference data with derived counters (checked-in vs total,
 * pending sync) computed by the repository.
 */
data class ExamPaperDetail(
    val paper: Paper,
    val center: Center,
    val totalCandidates: Int,
    val checkedInCount: Int,
    val lastSyncAt: Long?,
    val pendingSyncCount: Int,
)
