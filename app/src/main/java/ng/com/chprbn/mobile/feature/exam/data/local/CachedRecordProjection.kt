package ng.com.chprbn.mobile.feature.exam.data.local

/**
 * Flat row shape returned by the Cached Records queries on the exam
 * side. Attendance and remark rows are read together (`UNION ALL`) so
 * the screen can present one time-ordered list per tab without the
 * repo doing another pass. The `recordType` discriminator carries the
 * source table so the domain mapper can pick the right enum.
 *
 * All fields are non-null except [syncError]/[lastAttemptAt] because
 * a row that has NEVER been attempted has no error and no timestamp.
 */
data class CachedRecordProjection(
    /**
     * Discriminator: `"Attendance"` or `"Remark"`. Read as a
     * [ng.com.chprbn.mobile.feature.exam.domain.model.RecordType]
     * by the repo.
     */
    val recordType: String,
    val candidateId: String,
    val candidateName: String,
    val examNumber: String,
    val paperId: String,
    val paperTitle: String,
    val syncStatus: String,
    val syncError: String?,
    val capturedAt: Long,
    val lastAttemptAt: Long?,
)
