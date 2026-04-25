package ng.com.chprbn.mobile.feature.report.data.dto

/**
 * Mobile API v1 envelope for POST [ng.com.chprbn.mobile.feature.report.data.api.IrregularityReportApiService.submitIrregularityReport].
 */
data class IrregularityReportEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: IrregularityReportResponseDataDto? = null
)

data class IrregularityReportResponseDataDto(
    val id: Long? = null,
    val license_number: String? = null,
    val remark: String? = null
)
