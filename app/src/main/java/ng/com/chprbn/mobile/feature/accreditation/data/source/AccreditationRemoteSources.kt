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

/**
 * Full draft payload pushed via `POST accreditation/assessments/{id}/sync`.
 */
data class AccreditationDraftSyncPayload(
    val assessmentId: String,
    val institutionId: String,
    val cycleLabel: String,
    val facilityCode: String,
    val locationLabel: String,
    val updatedAt: Long,
    val sections: List<AccreditationSectionSyncPart>,
    val criteriaPayloadJson: String?,
    val criteriaEvaluatedCount: Int,
    val criteriaTotal: Int,
    val criteriaStatus: String?,
    val scoresPayloadJson: String?,
    val scoresCompletedCount: Int,
    val scoresTotalGroups: Int,
    val scoresStatus: String?,
    val attachments: List<AccreditationAttachmentSyncMeta>,
)

data class AccreditationSectionSyncPart(
    val sectionKey: String,
    val status: String,
    val payloadJson: String,
    val itemsCompleted: Int,
    val totalItems: Int,
    val codeLabel: String?,
    val rangeLabel: String?,
    val title: String?,
    val subtitle: String?,
)

data class AccreditationAttachmentSyncMeta(
    val id: String,
    val parentKey: String,
    val title: String,
    val isPhoto: Boolean,
    val sizeLabel: String?,
    val locationLabel: String?,
    val uploadState: String?,
    val localPath: String,
)

interface AccreditationSyncRemoteSource {
    /**
     * Uploads pending binary attachments (multipart) then posts the draft sync body.
     * Returns true when the server accepts the sync.
     */
    suspend fun uploadDraft(payload: AccreditationDraftSyncPayload): Boolean
}

interface AssessmentSubmissionRemoteSource {
    suspend fun submit(assessmentId: String): String

    suspend fun getDecision(decisionId: String): AssessmentDecision?
}
