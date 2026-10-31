package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class SaveCriteriaChecklistDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(
        institutionId: String,
        payloadJson: String,
        evaluatedCount: Int,
        totalCriteria: Int,
        status: DraftPersistenceStatus = DraftPersistenceStatus.SavedLocally,
    ): DraftOpResult {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.saveCriteria(
            assessmentId = draft.id,
            payloadJson = payloadJson,
            evaluatedCount = evaluatedCount,
            totalCriteria = totalCriteria,
            status = status,
        )
    }
}
