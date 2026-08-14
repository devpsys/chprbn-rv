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
    // Live backend sends "indexing" (e.g. "B/213/104/14"), not "exam_number".
    @SerializedName(value = "exam_number", alternate = ["indexing"]) val examNumber: String? = null,
    // Live backend sends "fullname" (no underscore), not "full_name".
    @SerializedName(value = "full_name", alternate = ["fullname"]) val fullName: String? = null,
    // Live backend sends "photo" (still raw Base64), not "photo_url".
    @SerializedName(value = "photo_url", alternate = ["photo"]) val photoUrl: String? = null,
)

data class PaperCandidateAssignmentDto(
    @SerializedName("paper_id") val paperId: String? = null,
    @SerializedName("candidate_id") val candidateId: String? = null,
)
