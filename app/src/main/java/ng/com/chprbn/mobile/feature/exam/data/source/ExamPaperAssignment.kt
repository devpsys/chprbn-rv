package ng.com.chprbn.mobile.feature.exam.data.source

/**
 * Transport-shape join between a paper and a candidate, used by the
 * dossier remote source to carry the M:N assignment data through to the
 * repository for persistence.
 *
 * Lives in `data.source` (not `domain.model`) because it has no
 * use-case-facing role — use cases query candidates and attendance, both
 * of which are derived from / joined with this data. Mirrors how the
 * assessment-side `AssessmentPackageBundle` is colocated with its source.
 *
 * [scheduledCandidateId] and [scheduleId] are required to push attendance
 * (`docs/mobile-api-guide.html` §5) — resolved at sync time via
 * `CandidateDao.getAssignment`, not stored on the `attendance` row itself
 * (keeps that table's schema untouched by this — see `AttendanceSyncHandler`).
 */
data class ExamPaperAssignment(
    val paperId: String,
    val candidateId: String,
    val scheduledCandidateId: String,
    val scheduleId: String,
)
