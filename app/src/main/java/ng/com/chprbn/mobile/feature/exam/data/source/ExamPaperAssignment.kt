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
 */
data class ExamPaperAssignment(
    val paperId: String,
    val candidateId: String,
)
