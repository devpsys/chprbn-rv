package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class DiscardLocalDraftsUseCase @Inject constructor(
    private val repository: AssessmentDraftRepository,
) {
    suspend operator fun invoke(assessmentIds: List<String>): DraftOpResult =
        repository.discard(assessmentIds)
}
