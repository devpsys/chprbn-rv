package ng.com.chprbn.mobile.feature.report.domain.model

/**
 * Domain payload for submitting a license irregularity report.
 * [snapshotContentUri] is a `content://` or `file://` URI string from the image picker.
 */
data class ReportLicenseIrregularityPayload(
    val nameOnCard: String,
    val licenseNumber: String,
    val cadre: String,
    val gender: String,
    val remark: IrregularityRemark,
    val snapshotContentUri: String,
    val reportedAtEpochMillis: Long
)
