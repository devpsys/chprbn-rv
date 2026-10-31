package ng.com.chprbn.mobile.feature.accreditation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.source.AssessmentSubmissionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetDecisionResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SubmitAssessmentResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentSubmissionRepository
import ng.com.chprbn.mobile.core.sync.Clock
import javax.inject.Inject

class AssessmentSubmissionRepositoryImpl @Inject constructor(
    private val remote: AssessmentSubmissionRemoteSource,
    private val draftDao: AssessmentDraftDao,
    private val clock: Clock,
) : AssessmentSubmissionRepository {

    override suspend fun submit(assessmentId: String): SubmitAssessmentResult =
        withContext(Dispatchers.IO) {
            try {
                val decisionId = remote.submit(assessmentId)
                draftDao.updateStatus(
                    assessmentId,
                    DraftPersistenceStatus.Synced.name,
                    clock.nowMillis(),
                )
                SubmitAssessmentResult.Success(decisionId)
            } catch (t: Throwable) {
                SubmitAssessmentResult.Error(t.message ?: "Unable to submit assessment.")
            }
        }

    override suspend fun getDecision(decisionId: String): GetDecisionResult =
        withContext(Dispatchers.IO) {
            try {
                val decision = remote.getDecision(decisionId)
                if (decision == null) {
                    GetDecisionResult.NotFound(decisionId)
                } else {
                    GetDecisionResult.Success(decision)
                }
            } catch (t: Throwable) {
                GetDecisionResult.Error(t.message ?: "Unable to load assessment decision.")
            }
        }
}
