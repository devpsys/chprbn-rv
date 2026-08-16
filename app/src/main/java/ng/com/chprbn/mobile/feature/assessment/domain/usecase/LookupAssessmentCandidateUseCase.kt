package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import javax.inject.Inject

/**
 * Resolves a scanned QR payload (or manually-entered indexing number) to a
 * candidate within a schedule's roster.
 *
 * [scannedPayload] is the QR-extracted exam/registration number — the
 * `AssessmentScan` nav flow passes this value through to downstream
 * screens under the arg name `candidateId` for legacy reasons, but the
 * shape is always the practitioner's registration number, not a DB id.
 * This use case does the exam-number → domain-`Candidate` translation
 * once so the ViewModel can key subsequent reads and writes on the real
 * DB id.
 *
 * Returns `null` when the number isn't assigned to this schedule; the UI
 * then surfaces a "candidate not on this schedule" error state.
 */
class LookupAssessmentCandidateUseCase @Inject constructor(
    private val repository: AssessmentCandidateRepository,
) {
    suspend operator fun invoke(scheduleId: String, scannedPayload: String): Candidate? {
        val schedule = scheduleId.trim()
        val examNumber = scannedPayload.trim()
        if (schedule.isEmpty() || examNumber.isEmpty()) return null
        return repository.getCandidateByExamNumber(schedule, examNumber)
    }
}
