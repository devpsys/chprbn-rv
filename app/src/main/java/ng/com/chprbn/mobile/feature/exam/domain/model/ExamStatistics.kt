package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Read-side aggregates backing `ExamStatisticsScreen`. Every field is
 * computed in SQL from local rows; the screen renders the counters and
 * the cached-vs-synced bar chart from this shape alone.
 *
 * [attendanceCaptured] and [projectCaptured] are row totals (one per
 * candidate per paper). [practicalCaptured] is distinct candidates
 * assessed (a candidate with many question scores still counts as one).
 * The cached / synced / pending / failed buckets sum the underlying
 * rows — including per-question practical scores — so they match what
 * Sync Now flushes through the batch runner.
 *
 * [lastUpdatedAt] tracks the most recent local write (not the last
 * successful sync), so the screen's "Updated 5m ago" pill reflects the
 * freshness of the data the user sees.
 */
data class ExamStatistics(
    val recordsDownloaded: Int,
    val attendanceCaptured: Int,
    val practicalCaptured: Int,
    val projectCaptured: Int,
    val syncedCount: Int,
    val cachedCount: Int,
    val pendingCount: Int,
    val failedCount: Int,
    val lastUpdatedAt: Long?,
)
