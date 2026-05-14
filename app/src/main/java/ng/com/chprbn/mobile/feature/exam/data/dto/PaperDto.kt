package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

data class PaperDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("center_id") val centerId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    /** Wire string for [ng.com.chprbn.mobile.core.domain.model.PaperKind]. */
    @SerializedName("paper_kind") val paperKind: String? = null,
    /** Epoch millis (UTC). */
    @SerializedName("start_at") val startAt: Long? = null,
    @SerializedName("end_at") val endAt: Long? = null,
    @SerializedName("hall") val hall: String? = null,
    @SerializedName("total_candidates") val totalCandidates: Int? = null,
)
