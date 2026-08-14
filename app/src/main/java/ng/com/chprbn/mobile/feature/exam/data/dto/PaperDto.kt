package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Confirmed against a real device response (2026-08-14) — the live
 * shape is just `id`/`code`/`name`. No `center_id` (the whole dossier is
 * already scoped to one centre), no kind/hall/timing, no candidate
 * count. [ng.com.chprbn.mobile.feature.exam.data.source.ApiExamDossierRemoteSource]
 * supplies `centerId` from the dossier's own centre and derives
 * `totalCandidates` from the assignment list; kind/hall/timing have no
 * wire signal at all and fall back to their mapper defaults.
 */
data class PaperDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
)
