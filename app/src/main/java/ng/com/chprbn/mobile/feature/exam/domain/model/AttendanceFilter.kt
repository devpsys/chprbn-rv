package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Filter applied to the candidates list on `ExamCandidatesScreen`. The
 * `All` value (the default) returns every row regardless of attendance
 * state; the others narrow to one specific state.
 *
 * Mirrored by the presentation layer's filter chip enum.
 */
enum class AttendanceFilter { All, SignedIn, SignedOut, Flagged }
