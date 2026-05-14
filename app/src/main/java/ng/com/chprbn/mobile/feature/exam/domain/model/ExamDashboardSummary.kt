package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Aggregated read for the Exam Dashboard top of the screen. Bundles the
 * officer's session, the centre header, and two task summaries
 * (Attendance + Practical) that drive the two action cards on the
 * screen.
 */
data class ExamDashboardSummary(
    val session: OfficerSession,
    val center: Center,
    val attendanceCard: ExamTaskSummary,
    val practicalCard: ExamTaskSummary,
)

/**
 * Per-card chip + count rendered on the Exam Dashboard. The labels are
 * domain strings (`"Active Session"`, `"Pending Grading"`); the screen
 * picks the icon/colour from the label content.
 */
data class ExamTaskSummary(
    val statusLabel: String,
    val countLabel: String,
)
