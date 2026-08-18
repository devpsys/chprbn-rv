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
    /** Papers ("schedules") scheduled for this center today. Zero is valid. */
    val papersCount: Int = 0,
    /**
     * `true` when the dossier contains at least one Theory paper —
     * attendance is a theory-paper concept (PE/PA route through the
     * scan → sections flow instead). Gates the Attendance card. Derived
     * from the local `papers` table by
     * [ng.com.chprbn.mobile.feature.exam.data.repository.ExamPaperRepositoryImpl.getDashboardSummary]
     * (`paper.paperKind == PaperKind.Theory`), never from string-matching
     * the wire `code` — `PaperKind.fromWireCode` is the single source of
     * truth for that mapping.
     */
    val hasAttendancePapers: Boolean = false,
    /**
     * `true` when the dossier carried practical section reference data.
     * Mirrors [Center.hasSections] (kept there because the wire field
     * has no local table equivalent) and gates the Practical card. Also
     * exposed here so the dashboard code path doesn't have to know
     * about [Center.hasSections] internals.
     */
    val hasPracticalAssessment: Boolean = false,
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
