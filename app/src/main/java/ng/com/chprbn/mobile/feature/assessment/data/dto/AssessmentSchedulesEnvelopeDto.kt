package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** No backend endpoint for this exists yet — see
 * `docs/exam-assessment-clean-architecture-plan.md` §12 (Critical action
 * C1). The shape mirrors verification's `LicenseRecordEnvelopeDto` so the
 * mapper pattern stays uniform. Revisit once the contract is signed.
 *
 * Every field is nullable with a default to keep the parser tolerant: the
 * mapper applies fallbacks rather than crashing on a missing key.
 */
data class AssessmentSchedulesEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<AssessmentScheduleDto>? = null,
)

data class AssessmentScheduleDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    /** Epoch millis (UTC). */
    @SerializedName("date") val date: Long? = null,
    /** Wire string for [ng.com.chprbn.mobile.core.domain.model.PaperKind]. */
    @SerializedName("paper_kind") val paperKind: String? = null,
    @SerializedName("center_id") val centerId: String? = null,
)
