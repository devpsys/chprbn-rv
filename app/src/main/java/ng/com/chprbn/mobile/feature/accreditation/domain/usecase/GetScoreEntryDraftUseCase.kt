package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class GetScoreEntryDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(institutionId: String): ScoreDraftRecord? {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.getScores(draft.id)
    }
}
