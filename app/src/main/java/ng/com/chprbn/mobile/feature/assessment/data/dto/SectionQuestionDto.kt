package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

data class SectionQuestionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("section_id") val sectionId: String? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("prompt") val prompt: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("max_score") val maxScore: Int? = null,
)
