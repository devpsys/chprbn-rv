package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class GetSsqSectionDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(
        institutionId: String,
        sectionKey: SsqSectionKey,
    ): SsqSectionDraftRecord? {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.getSection(draft.id, sectionKey)
    }
}
