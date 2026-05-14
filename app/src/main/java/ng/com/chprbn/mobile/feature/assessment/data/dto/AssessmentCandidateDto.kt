package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Candidate identity as it appears in the package payload. The shape
 * matches the cross-feature `core.domain.model.Candidate`.
 *
 * [ScheduleCandidateAssignmentDto] is colocated because it's always
 * parsed alongside the candidate list as part of the same package.
 */
data class AssessmentCandidateDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("exam_number") val examNumber: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
)

data class ScheduleCandidateAssignmentDto(
    @SerializedName("schedule_id") val scheduleId: String? = null,
    @SerializedName("candidate_id") val candidateId: String? = null,
)
