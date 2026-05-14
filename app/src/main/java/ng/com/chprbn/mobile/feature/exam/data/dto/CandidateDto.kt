package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Candidate identity as it appears in the dossier payload. The shape
 * matches the cross-feature `core.domain.model.Candidate`.
 *
 * [PaperCandidateAssignmentDto] is colocated because it's always
 * parsed alongside the candidate list as part of the same dossier.
 */
data class CandidateDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("exam_number") val examNumber: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
)

data class PaperCandidateAssignmentDto(
    @SerializedName("paper_id") val paperId: String? = null,
    @SerializedName("candidate_id") val candidateId: String? = null,
)
