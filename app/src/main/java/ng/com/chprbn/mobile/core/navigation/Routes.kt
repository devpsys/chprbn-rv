package ng.com.chprbn.mobile.core.navigation

import android.net.Uri

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val Profile = "profile"
    const val Scan = "scan"
    const val ManualLicenseEntry = "manual_license_entry"
    const val Sync = "sync"
    const val Verified = "verified"
    /** Optional: practitionerName, licenseNumber. Use [verificationFormRoute] to build with args. */
    const val VerificationForm = "verification_form?practitionerName={practitionerName}&licenseNumber={licenseNumber}"
    fun verificationFormRoute(practitionerName: String = "", licenseNumber: String = ""): String =
        "verification_form?practitionerName=${Uri.encode(practitionerName)}&licenseNumber=${Uri.encode(licenseNumber)}"
    const val SyncHistory = "sync_history"

    /** Route pattern for record detail; use [recordDetailRoute] to build with argument. */
    const val RecordDetail = "record_detail/{registrationNumber}"

    fun recordDetailRoute(registrationNumber: String): String =
        "record_detail/${Uri.encode(registrationNumber)}"
}
