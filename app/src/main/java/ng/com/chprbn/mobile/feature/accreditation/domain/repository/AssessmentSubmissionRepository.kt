package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetDecisionResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SubmitAssessmentResult

interface AssessmentSubmissionRepository {
    suspend fun submit(assessmentId: String): SubmitAssessmentResult

    suspend fun getDecision(decisionId: String): GetDecisionResult
}
