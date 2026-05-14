package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

data class AssessmentPaperDto(
    @SerializedName("schedule_id") val scheduleId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("status_label") val statusLabel: String? = null,
    @SerializedName("facility_name") val facilityName: String? = null,
    @SerializedName("facility_address") val facilityAddress: String? = null,
    @SerializedName("hall_name") val hallName: String? = null,
    @SerializedName("hall_address") val hallAddress: String? = null,
    @SerializedName("hero_image_url") val heroImageUrl: String? = null,
)
