package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.Candidate

/**
 * One row on the Exam Candidates list. Bundles the cross-feature
 * [Candidate] identity with this paper's `attendance` row (null when the
 * examiner hasn't marked the candidate yet) and a count of remarks for
 * the "1 Remark" pill.
 */
data class ExamCandidateRow(
    val candidate: Candidate,
    val attendance: Attendance?,
    val remarkCount: Int,
)
