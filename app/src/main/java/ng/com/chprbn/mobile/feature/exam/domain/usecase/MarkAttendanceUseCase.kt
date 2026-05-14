package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import javax.inject.Inject

/**
 * Single write path for attendance. Backs the "Mark Attendance" FAB on
 * the scan-result screen and the per-row check-in toggles on the
 * candidates list.
 */
class MarkAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(
        paperId: String,
        candidateId: String,
        status: AttendanceStatus,
    ): MarkAttendanceResult {
        val trimmedPaper = paperId.trim()
        val trimmedCandidate = candidateId.trim()
        if (trimmedPaper.isEmpty() || trimmedCandidate.isEmpty()) {
            return MarkAttendanceResult.Error("Paper and candidate are required.")
        }
        return repository.markAttendance(trimmedPaper, trimmedCandidate, status)
    }
}
