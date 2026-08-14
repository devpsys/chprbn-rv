package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Pulls the officer's day in one payload. Confirmed against a real
 * device response (2026-08-14) — the shape diverges substantially from
 * what was originally speculated (see git history on this file for the
 * old flat `papers`/`candidates`/`assignments` guess):
 *
 * - `data.centre` (British spelling), not `data.center`.
 * - `data.papers[]` is real but minimal — just `id`/`code`/`name`, no
 *   kind/hall/timing/candidate-count. Those live elsewhere or don't
 *   exist on the wire at all.
 * - There is no top-level `data.candidates` or `data.assignments`.
 *   Both live nested per [ScheduleDto] instead: `candidates[]` is the
 *   roster for that schedule, `paper_candidates[]` is the candidate↔paper
 *   join (reuses [PaperCandidateAssignmentDto] — its `paper_id`/`candidate_id`
 *   keys already matched this shape exactly).
 *
 * [ApiExamDossierRemoteSource][ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource]
 * flattens `schedules[].candidates`/`schedules[].paper_candidates` across
 * every schedule into the bundle's flat candidate/assignment lists, and
 * derives each paper's candidate count from the assignment list since
 * the wire doesn't provide one directly.
 */
data class ExamDossierEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"]) val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExamDossierDataDto? = null,
)

data class ExamDossierDataDto(
    @SerializedName(value = "center", alternate = ["centre"]) val center: CenterDto? = null,
    @SerializedName("papers") val papers: List<PaperDto>? = null,
    @SerializedName("schedules") val schedules: List<ScheduleDto>? = null,
    /**
     * Always empty in every real response seen so far — element shape is
     * unconfirmed, so this is parsed generically via [JsonElement] rather
     * than a concrete DTO. [ApiExamDossierRemoteSource][ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource]
     * only checks non-emptiness (drives the dashboard's Practical
     * Assessment card via `Center.hasSections`) — it never reads into an
     * individual element.
     */
    @SerializedName("sections") val sections: List<JsonElement>? = null,
)

/**
 * One exam sitting for this centre: a date + qualification code
 * (`test_code`, e.g. `"CHEW"`) carrying its own candidate roster and
 * paper assignments. Not the same concept as [PaperDto] — a schedule can
 * reference multiple papers via [paperCandidates].
 */
data class ScheduleDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("test_date") val testDate: String? = null,
    @SerializedName("test_code") val testCode: String? = null,
    @SerializedName("test_type") val testType: String? = null,
    @SerializedName("paper_candidates") val paperCandidates: List<PaperCandidateAssignmentDto>? = null,
    @SerializedName("candidates") val candidates: List<CandidateDto>? = null,
)
