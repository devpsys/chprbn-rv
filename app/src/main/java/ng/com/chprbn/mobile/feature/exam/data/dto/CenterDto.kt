package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

data class CenterDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("hero_image_url") val heroImageUrl: String? = null,
)
