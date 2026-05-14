package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Pulls a schedule's entire static reference data in one
 * payload. No backend endpoint exists yet (plan §12, C1).
 *
 * Splits paper / sections / questions / candidates / assignments into
 * separate arrays so the wire is normalised; the repository fans them out
 * into the per-table inserts inside `db.withTransaction`.
 */
data class AssessmentPackageEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AssessmentPackageDataDto? = null,
)

data class AssessmentPackageDataDto(
    @SerializedName("paper") val paper: AssessmentPaperDto? = null,
    @SerializedName("sections") val sections: List<PracticalSectionDto>? = null,
    @SerializedName("questions") val questions: List<SectionQuestionDto>? = null,
    @SerializedName("candidates") val candidates: List<AssessmentCandidateDto>? = null,
    @SerializedName("assignments") val assignments: List<ScheduleCandidateAssignmentDto>? = null,
)
