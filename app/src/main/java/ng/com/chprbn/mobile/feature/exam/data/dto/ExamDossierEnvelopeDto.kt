package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** No backend endpoint for this exists yet — see
 * `docs/exam-assessment-clean-architecture-plan.md` §12 (Critical action
 * C1). The shape mirrors the assessment-side package envelope so the
 * mapper + remote-source patterns stay uniform.
 *
 * Pulls the officer's day in one payload: centre, the day's papers,
 * the candidate roster, and the assignment join. Splitting them on the
 * wire (vs. one nested hierarchy) keeps the response normalised and the
 * mapper's job a flat fan-out.
 */
data class ExamDossierEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExamDossierDataDto? = null,
)

data class ExamDossierDataDto(
    @SerializedName("center") val center: CenterDto? = null,
    @SerializedName("papers") val papers: List<PaperDto>? = null,
    @SerializedName("candidates") val candidates: List<CandidateDto>? = null,
    @SerializedName("assignments") val assignments: List<PaperCandidateAssignmentDto>? = null,
)
