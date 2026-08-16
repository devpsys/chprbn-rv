package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Practical-assessment section as it arrives on the exam dossier. Sections
 * are top-level on `data.sections[]`, not nested under a paper — a dossier
 * carries at most one active practical paper's worth of sections (fanned
 * out by [ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource]
 * to every PE/PA paper on the response).
 *
 * Wire ids are integers (`1`, `2`, …); the assessment schema uses String
 * ids so the mapper stringifies them. `status` is currently unused by
 * mobile — the server has a soft-delete signal here, but every response
 * seen so far is `1` (active). Rows with `status != 1` should be skipped
 * once the server actually toggles it.
 */
data class PracticalSectionWireDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("questions") val questions: List<SectionQuestionWireDto>? = null,
)

/**
 * One scoreable prompt inside a [PracticalSectionWireDto]. `mark` is the
 * maximum score for the question (mapped to `SectionQuestion.maxScore`).
 * `section_id` echoes the parent id — the mapper trusts the nesting and
 * ignores it, so a mis-numbered `section_id` on a genuinely-nested
 * question won't strand the row.
 */
data class SectionQuestionWireDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("section_id") val sectionId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mark") val mark: Int? = null,
    @SerializedName("status") val status: Int? = null,
)
