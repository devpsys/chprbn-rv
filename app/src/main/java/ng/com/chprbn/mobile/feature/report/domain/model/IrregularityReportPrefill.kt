package ng.com.chprbn.mobile.feature.report.domain.model

/**
 * Small JSON-safe payload for navigation (avoids huge license photo strings in route args).
 */
data class IrregularityReportPrefill(
    val nameOnCard: String = "",
    val licenseNumber: String = "",
    val cadre: String = "",
    val gender: String = ""
)
