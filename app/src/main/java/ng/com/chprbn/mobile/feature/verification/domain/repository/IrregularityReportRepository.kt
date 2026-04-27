package ng.com.chprbn.mobile.feature.verification.domain.repository

import ng.com.chprbn.mobile.feature.verification.domain.model.ReportLicenseIrregularityPayload
import ng.com.chprbn.mobile.feature.verification.domain.model.SubmitIrregularityReportResult

/**
 * Persists or uploads irregularity reports. Replace [IrregularityReportRepositoryImpl] with a remote-backed implementation when the API is ready.
 */
interface IrregularityReportRepository {
    suspend fun submitReport(payload: ReportLicenseIrregularityPayload): SubmitIrregularityReportResult
}
