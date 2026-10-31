package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class SaveScoreEntryDraftUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(
        institutionId: String,
        payloadJson: String,
        completedCount: Int,
        totalGroups: Int,
        status: DraftPersistenceStatus = DraftPersistenceStatus.SavedLocally,
    ): DraftOpResult {
        val draft = repository.getOrCreateCurrent(institutionId)
        return repository.saveScores(
            assessmentId = draft.id,
            payloadJson = payloadJson,
            completedCount = completedCount,
            totalGroups = totalGroups,
            status = status,
        )
    }
}
