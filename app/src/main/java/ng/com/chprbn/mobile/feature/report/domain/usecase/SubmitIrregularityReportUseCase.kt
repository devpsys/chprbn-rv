package ng.com.chprbn.mobile.feature.report.domain.usecase

import ng.com.chprbn.mobile.feature.report.domain.model.IrregularityRemark
import ng.com.chprbn.mobile.feature.report.domain.model.ReportLicenseIrregularityPayload
import ng.com.chprbn.mobile.feature.report.domain.model.SubmitIrregularityReportResult
import ng.com.chprbn.mobile.feature.report.domain.repository.IrregularityReportRepository
import javax.inject.Inject

class SubmitIrregularityReportUseCase @Inject constructor(
    private val repository: IrregularityReportRepository
) {
    suspend operator fun invoke(
        nameOnCard: String,
        licenseNumber: String,
        cadre: String,
        gender: String,
        remark: IrregularityRemark?,
        snapshotContentUri: String?,
        reportedAtEpochMillis: Long = System.currentTimeMillis()
    ): SubmitIrregularityReportResult {
        val name = nameOnCard.trim()
        val license = licenseNumber.trim()
        val cadreTrim = cadre.trim()
        val genderTrim = gender.trim()
        val uri = snapshotContentUri?.trim().orEmpty()

        if (name.isEmpty()) return SubmitIrregularityReportResult.Error("Name on card is required.")
        if (license.isEmpty()) return SubmitIrregularityReportResult.Error("License number is required.")
        if (cadreTrim.isEmpty()) return SubmitIrregularityReportResult.Error("Cadre is required.")
        if (genderTrim.isEmpty()) return SubmitIrregularityReportResult.Error("Gender is required.")
        if (remark == null) return SubmitIrregularityReportResult.Error("Please select a remark.")
        if (uri.isEmpty()) return SubmitIrregularityReportResult.Error("Please attach a snapshot of the license.")

        val payload = ReportLicenseIrregularityPayload(
            nameOnCard = name,
            licenseNumber = license,
            cadre = cadreTrim,
            gender = genderTrim,
            remark = remark,
            snapshotContentUri = uri,
            reportedAtEpochMillis = reportedAtEpochMillis
        )
        return repository.submitReport(payload)
    }
}
