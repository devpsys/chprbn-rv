package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

data class PracticalSectionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("schedule_id") val scheduleId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("ordering") val ordering: Int? = null,
)
