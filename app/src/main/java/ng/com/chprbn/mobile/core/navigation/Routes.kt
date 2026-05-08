package ng.com.chprbn.mobile.core.navigation

import android.net.Uri

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val ExamDashboard = "exam_dashboard"
    const val ExamPapers = "exam_papers"
    const val ExamStatistics = "exam_statistics"
    const val ExamPaper = "exam_paper"
    const val ExamCandidates = "exam_candidates"
    const val ExamScan = "exam_scan"
    const val Verification = "verification"
    const val Profile = "profile"
    const val Scan = "scan"
    /** Query [forExam]: exam attendance uses indexing copy; verification uses license copy. */
    const val ManualLicenseEntry = "manual_license_entry?forExam={forExam}"

    fun manualLicenseEntryRoute(forExam: Boolean): String =
        "manual_license_entry?forExam=$forExam"
    const val Sync = "sync"
    const val Verified = "verified"

    /** Optional: licenseRecordJson (Uri-encoded JSON). Use [verificationFormRoute] to build with [ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord]. */
    const val VerificationForm = "verification_form?licenseRecordJson={licenseRecordJson}"

    /** Builds route with full record as single source of truth. Encode [licenseRecordJson] with [Uri.encode] before calling. */
    fun verificationFormRoute(licenseRecordJson: String): String =
        "verification_form?licenseRecordJson=$licenseRecordJson"

    /** Prefill JSON (name, license, cadre, gender only); use [reportIrregularityRoute] with [Uri.encode]. */
    const val ReportIrregularity = "report_irregularity?prefillJson={prefillJson}"

    fun reportIrregularityRoute(prefillJson: String): String =
        "report_irregularity?prefillJson=$prefillJson"

    const val SyncHistory = "sync_history"

    /** Route pattern for record detail; use [recordDetailRoute] to build with argument. */
    const val RecordDetail = "record_detail/{registrationNumber}"

    fun recordDetailRoute(registrationNumber: String): String =
        "record_detail/${Uri.encode(registrationNumber)}"

    const val CandidateScanResult = "candidate_scan_result/{scannedPayload}"

    fun candidateScanResultRoute(scannedPayload: String): String =
        "candidate_scan_result/${Uri.encode(scannedPayload)}"
}
