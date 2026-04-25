package ng.com.chprbn.mobile.feature.report.domain.repository

import ng.com.chprbn.mobile.feature.report.domain.model.ReportLicenseIrregularityPayload
import ng.com.chprbn.mobile.feature.report.domain.model.SubmitIrregularityReportResult

/**
 * Persists or uploads irregularity reports. Replace [IrregularityReportRepositoryImpl] with a remote-backed implementation when the API is ready.
 */
interface IrregularityReportRepository {
    suspend fun submitReport(payload: ReportLicenseIrregularityPayload): SubmitIrregularityReportResult
}
