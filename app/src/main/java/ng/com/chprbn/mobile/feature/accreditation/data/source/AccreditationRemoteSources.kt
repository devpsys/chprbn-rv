package ng.com.chprbn.mobile.feature.accreditation.data.source

import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationHubData
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDecision
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionProfile
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord

interface AccreditationAssignmentRemoteSource {
    suspend fun getHub(): AccreditationHubData
}

interface InstitutionRemoteSource {
    suspend fun getProfile(institutionId: String): InstitutionProfile?
}

/**
 * Seed payloads for first-open of an institution assessment.
 * [payloadJson] fields may be empty — presentation falls back to placeholders.
 */
data class AssessmentSeedBundle(
    val draft: AssessmentDraftRecord,
    val sections: List<SsqSectionDraftRecord>,
    val criteria: CriteriaDraftRecord,
    val scores: ScoreDraftRecord,
)

interface AccreditationDraftRemoteSource {
    suspend fun getSeedBundle(institutionId: String): AssessmentSeedBundle?

    suspend fun sampleLocalDrafts(): List<LocalDraftListItem>
}

interface AccreditationSyncRemoteSource {
    suspend fun upload(assessmentId: String): Boolean
}

interface AssessmentSubmissionRemoteSource {
    suspend fun submit(assessmentId: String): String

    suspend fun getDecision(decisionId: String): AssessmentDecision?
}
