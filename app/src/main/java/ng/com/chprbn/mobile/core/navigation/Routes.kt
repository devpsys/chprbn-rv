package ng.com.chprbn.mobile.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route declarations. Each destination is a @Serializable
 * Kotlin object (no args) or data class (with args). Compose Nav 2.8+ generates
 * the route patterns and arg encoding from the @Serializable descriptors, so:
 *
 *   * adding/removing a field on a route class produces a compile error at every
 *     navigate() call — no more silent route-string drift,
 *   * args are typed (Boolean, String, etc.), eliminating the previous Gson-via-
 *     query-string round-trip and the manual Uri.encode/decode plumbing,
 *   * the destination's `SavedStateHandle` continues to expose each property by
 *     its name as a `String` (or `Boolean`, etc.), so existing ViewModel
 *     extraction code (`savedStateHandle.get<String>("registrationNumber")`) is
 *     unchanged.
 */
object Routes {
    @Serializable
    data object Splash

    @Serializable
    data object Login

    @Serializable
    data object Dashboard

    @Serializable
    data object ExamDashboard

    @Serializable
    data object ExamPapers

    @Serializable
    data object ExamStatistics

    @Serializable
    data object ExamPaper

    @Serializable
    data object ExamCandidates

    @Serializable
    data object ExamScan

    @Serializable
    data object Verification

    @Serializable
    data object Profile

    @Serializable
    data object Scan

    /**
     * Manual license entry. [forExam] = true switches the copy from license
     * verification to exam attendance indexing.
     */
    @Serializable
    data class ManualLicenseEntry(val forExam: Boolean = false)

    @Serializable
    data object Sync

    @Serializable
    data object Verified

    /** VM re-fetches the license record by [registrationNumber] via GetLicenseRecordUseCase. */
    @Serializable
    data class VerificationForm(val registrationNumber: String)

    /** VM pre-populates the irregularity report form by re-fetching by [registrationNumber]. */
    @Serializable
    data class ReportIrregularity(val registrationNumber: String)

    @Serializable
    data object SyncHistory

    @Serializable
    data class RecordDetail(val registrationNumber: String)

    @Serializable
    data class CandidateScanResult(val scannedPayload: String)

    // ============== Assessment ==============
    /** Examination Schedules — first screen of the assessment feature. */
    @Serializable
    data object ExaminationSchedules

    /** Paper Detail for a specific schedule. [scheduleId] is captured by the
     *  destination VM and used to fetch the paper once a real data source
     *  exists; today the placeholder UiState is the same regardless. */
    @Serializable
    data class AssessmentPaperDetail(val scheduleId: String)

    /** Full candidates directory for a paper, reached from Paper Detail's
     *  "View Full Directory" button. [scheduleId] keys the lookup. */
    @Serializable
    data class AssessmentCandidates(val scheduleId: String)

    /** Assessment-side QR scan. Distinct from `ExamScan` so the scan result
     *  navigates into the practical-sections flow rather than the exam
     *  attendance result. */
    @Serializable
    data class AssessmentScan(val scheduleId: String)

    /** Practical Sections Hub, reached from the assessment scan once a
     *  candidate code has been read. */
    @Serializable
    data class AssessmentPracticalSections(
        val scheduleId: String,
        val candidateId: String,
    )

    /** Per-section scoring screen, reached by tapping a section card on
     *  the Practical Sections hub. [sectionId] picks the question set
     *  (e.g. "A", "B", "C"). */
    @Serializable
    data class AssessmentPracticalScoring(
        val scheduleId: String,
        val candidateId: String,
        val sectionId: String,
    )
}
