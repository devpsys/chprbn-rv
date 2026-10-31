package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class SaveAllSectionDraftsUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(institutionId: String): DraftOpResult {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.saveAllSections(draft.id)
    }
}
