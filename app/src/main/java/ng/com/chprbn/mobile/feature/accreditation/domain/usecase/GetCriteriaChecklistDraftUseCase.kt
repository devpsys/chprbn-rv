package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class GetCriteriaChecklistDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(institutionId: String): CriteriaDraftRecord? {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.getCriteria(draft.id)
    }
}
