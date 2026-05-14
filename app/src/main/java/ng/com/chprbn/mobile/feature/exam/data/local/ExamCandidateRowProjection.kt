package ng.com.chprbn.mobile.feature.exam.data.local

/**
 * Projection backing [CandidateDao.rowsForPaper]. Joins the candidate
 * roster with this paper's attendance row (nullable LEFT JOIN) plus a
 * per-candidate remark count, all in SQL so the JVM never sees the raw
 * tables.
 *
 * Mirrors the domain `ExamCandidateRow` shape but stays in the data
 * layer because `attendanceStatus` / `attendanceSyncStatus` arrive as
 * `String?` and need bridging through [toSyncStatus] / the enum
 * `valueOf`s.
 */
data class ExamCandidateRowProjection(
    val candidateId: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String?,
    val attendanceStatus: String?,
    val attendanceMarkedAt: Long?,
    val attendanceSyncStatus: String?,
    val attendanceSyncError: String?,
    val remarkCount: Int,
)
