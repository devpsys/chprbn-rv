package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Per-candidate attendance state for a single paper. `Flagged` is a
 * separate axis the officer can use to mark candidates who need
 * follow-up; it coexists with `SignedIn`/`SignedOut` but the design
 * filters treat it as a discrete state.
 */
enum class AttendanceStatus { SignedIn, SignedOut, Flagged }
