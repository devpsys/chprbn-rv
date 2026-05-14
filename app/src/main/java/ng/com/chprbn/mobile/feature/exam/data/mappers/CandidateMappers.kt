package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.local.ExamCandidateRowProjection
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow

internal fun CandidateEntity.toDomain(): Candidate = Candidate(
    id = id,
    examNumber = examNumber,
    fullName = fullName,
    photoUrl = photoUrl,
)

internal fun Candidate.toExamCandidateEntity(): CandidateEntity = CandidateEntity(
    id = id,
    examNumber = examNumber,
    fullName = fullName,
    photoUrl = photoUrl,
)

/**
 * Folds a [ExamCandidateRowProjection] (raw SQL result) into the domain
 * [ExamCandidateRow], resolving the LEFT-JOINed attendance fields into a
 * nullable [Attendance] (null when the candidate hasn't been marked).
 *
 * The candidate cannot resolve their own paperId from the projection
 * (the join sits on `pca.paperId = :paperId`), so the caller supplies it.
 */
internal fun ExamCandidateRowProjection.toDomain(paperId: String): ExamCandidateRow {
    val attendance = attendanceStatus?.let { statusStr ->
        Attendance(
            paperId = paperId,
            candidateId = candidateId,
            status = statusStr.toAttendanceStatus(),
            markedAt = attendanceMarkedAt ?: 0L,
            syncStatus = attendanceSyncStatus?.toSyncStatus() ?: SyncStatus.Pending,
            syncError = attendanceSyncError,
        )
    }
    return ExamCandidateRow(
        candidate = Candidate(
            id = candidateId,
            examNumber = examNumber,
            fullName = fullName,
            photoUrl = photoUrl,
        ),
        attendance = attendance,
        remarkCount = remarkCount,
    )
}
