package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

class GetPracticalSectionsUseCase @Inject constructor(
    private val repository: PracticalScoringRepository,
) {
    /**
     * Snapshot fetch. Kept alongside [observe] for callers that only need a
     * one-shot read (e.g. tests).
     */
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalSectionSummary> {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) return emptyList()
        return repository.getSections(schedule, candidate)
    }

    /**
     * Live section summary that re-emits every time the candidate's scores
     * change (A-S6 audit fix — the sections screen used to keep a stale
     * snapshot after a back-nav from the per-question scoring child).
     */
    fun observe(
        scheduleId: String,
        candidateId: String,
    ): Flow<List<PracticalSectionSummary>> {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) return flowOf(emptyList())
        return repository.observeSections(schedule, candidate)
    }
}
