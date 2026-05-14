package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

/**
 * Backs the "Save Scores" FAB on the practical-scoring screen. Per-question
 * scores are already persisted (each stepper tap flushes through
 * [RecordPracticalScoreUseCase]); this use case is the explicit gesture
 * that flags the section as ready to upload — enqueues sync jobs for every
 * pending row in the section and pops back to the hub on success.
 */
class CommitPracticalSectionUseCase @Inject constructor(
    private val repository: PracticalScoringRepository,
) {
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): SaveResult {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        val section = sectionId.trim()
        if (schedule.isEmpty() || candidate.isEmpty() || section.isEmpty()) {
            return SaveResult.Error("Schedule, candidate, and section are required.")
        }
        return repository.commitSection(schedule, candidate, section)
    }
}
