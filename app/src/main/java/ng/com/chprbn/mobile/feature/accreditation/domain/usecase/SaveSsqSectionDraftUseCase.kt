package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class SaveSsqSectionDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(
        institutionId: String,
        sectionKey: SsqSectionKey,
        payloadJson: String,
        itemsCompleted: Int,
        totalItems: Int,
        status: DraftPersistenceStatus = DraftPersistenceStatus.SavedLocally,
    ): DraftOpResult {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.saveSection(
            assessmentId = draft.id,
            sectionKey = sectionKey,
            payloadJson = payloadJson,
            status = status,
            itemsCompleted = itemsCompleted,
            totalItems = totalItems,
        )
    }
}
