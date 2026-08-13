package ng.com.chprbn.mobile.feature.verification.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Mobile API v1 envelope for POST [ng.com.chprbn.mobile.feature.verification.data.api.IrregularityReportApiService.submitIrregularityReport].
 */
data class IrregularityReportEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: IrregularityReportResponseDataDto? = null
)

data class IrregularityReportResponseDataDto(
    val id: Long? = null,
    val license_number: String? = null,
    val remark: String? = null
)
