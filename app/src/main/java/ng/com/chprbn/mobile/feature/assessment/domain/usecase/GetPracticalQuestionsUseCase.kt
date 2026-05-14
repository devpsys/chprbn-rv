package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

/**
 * Returns each question in a section together with the candidate's current
 * score for that question (`null` when not yet scored). The pairing avoids
 * a second query at the presentation seam.
 */
class GetPracticalQuestionsUseCase @Inject constructor(
    private val repository: PracticalScoringRepository,
) {
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): List<Pair<SectionQuestion, PracticalScore?>> {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        val section = sectionId.trim()
        if (schedule.isEmpty() || candidate.isEmpty() || section.isEmpty()) return emptyList()
        return repository.getQuestions(schedule, candidate, section)
    }
}
