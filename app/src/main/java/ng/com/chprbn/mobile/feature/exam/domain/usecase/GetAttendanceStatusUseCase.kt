package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import javax.inject.Inject

/**
 * Reads a candidate's current attendance status for a paper, if any.
 * Backs the scan-result screen's sign-in/sign-out toggle decision.
 */
class GetAttendanceStatusUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(paperId: String, candidateId: String): AttendanceStatus? =
        repository.getAttendance(paperId, candidateId)?.status
}
