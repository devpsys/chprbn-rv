package ng.com.chprbn.mobile.feature.exam.domain.model

sealed interface MarkAttendanceResult {
    data class Success(val attendance: Attendance) : MarkAttendanceResult
    data class Error(val message: String) : MarkAttendanceResult
}
