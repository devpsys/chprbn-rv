package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Examiner's currently-active centre + day. Materialised once at login (or
 * when the officer switches centres) and read by every exam screen to
 * scope queries to "this examiner's session".
 *
 * [dayIso] is the local ISO-8601 date (`"2026-06-12"`) the session is
 * bound to — the dossier download is keyed on it, and the dashboard's
 * "today" header reflects it.
 */
data class OfficerSession(
    val officerId: String,
    val centerId: String,
    val dayIso: String,
)
