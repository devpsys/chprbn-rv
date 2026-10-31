package ng.com.chprbn.mobile.feature.accreditation.data.sync

import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationAttachmentSyncMeta
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftSyncPayload
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSectionSyncPart
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AttachmentUploadState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import javax.inject.Inject

/**
 * Uploads pending accreditation assessment drafts (sections + criteria + scores +
 * attachment binaries) via [AccreditationSyncRemoteSource].
 * Entity key = assessment draft id.
 */
class AccreditationDraftSyncHandler @Inject constructor(
    private val draftDao: AssessmentDraftDao,
    private val sectionDao: SsqSectionDraftDao,
    private val criteriaDao: CriteriaDraftDao,
    private val scoreDao: ScoreDraftDao,
    private val attachmentDao: AccreditationAttachmentDao,
    private val remote: AccreditationSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        for (key in entityKeys) {
            val entity = draftDao.getById(key)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Drop
                continue
            }
            try {
                val payload = buildPayload(entity.id)
                val ok = remote.uploadDraft(payload)
                if (ok) {
                    draftDao.updateStatus(
                        id = key,
                        status = DraftPersistenceStatus.Synced.name,
                        updatedAt = clock.nowMillis(),
                    )
                    markAttachmentsUploaded(key)
                    outcomes[key] = SyncOutcome.Success
                } else {
                    draftDao.updateStatus(
                        id = key,
                        status = DraftPersistenceStatus.SyncFailed.name,
                        updatedAt = clock.nowMillis(),
                    )
                    outcomes[key] = SyncOutcome.Failure("Upload rejected for $key")
                }
            } catch (t: Throwable) {
                draftDao.updateStatus(
                    id = key,
                    status = DraftPersistenceStatus.SyncFailed.name,
                    updatedAt = clock.nowMillis(),
                )
                outcomes[key] = SyncOutcome.Failure(t.message ?: "Upload failed")
            }
        }
        return outcomes
    }

    private suspend fun buildPayload(assessmentId: String): AccreditationDraftSyncPayload {
        val draft = draftDao.getById(assessmentId)
            ?: error("Draft $assessmentId missing during payload build")
        val sections = sectionDao.getForAssessment(assessmentId).map { section ->
            AccreditationSectionSyncPart(
                sectionKey = section.sectionKey,
                status = section.status,
                payloadJson = section.payloadJson,
                itemsCompleted = section.itemsCompleted,
                totalItems = section.totalItems,
                codeLabel = section.codeLabel,
                rangeLabel = section.rangeLabel,
                title = section.title,
                subtitle = section.subtitle,
            )
        }
        val criteria = criteriaDao.get(assessmentId)
        val scores = scoreDao.get(assessmentId)
        val attachments = attachmentDao.listForAssessment(assessmentId).map { att ->
            AccreditationAttachmentSyncMeta(
                id = att.id,
                parentKey = att.parentKey,
                title = att.title,
                isPhoto = att.isPhoto,
                sizeLabel = att.sizeLabel,
                locationLabel = att.locationLabel,
                uploadState = att.uploadState,
                localPath = att.localPath,
            )
        }
        return AccreditationDraftSyncPayload(
            assessmentId = draft.id,
            institutionId = draft.institutionId,
            cycleLabel = draft.cycleLabel,
            facilityCode = draft.facilityCode,
            locationLabel = draft.locationLabel,
            updatedAt = draft.updatedAt,
            sections = sections,
            criteriaPayloadJson = criteria?.payloadJson,
            criteriaEvaluatedCount = criteria?.evaluatedCount ?: 0,
            criteriaTotal = criteria?.totalCriteria ?: 0,
            criteriaStatus = criteria?.status,
            scoresPayloadJson = scores?.payloadJson,
            scoresCompletedCount = scores?.completedCount ?: 0,
            scoresTotalGroups = scores?.totalGroups ?: 0,
            scoresStatus = scores?.status,
            attachments = attachments,
        )
    }

    private suspend fun markAttachmentsUploaded(assessmentId: String) {
        attachmentDao.listForAssessment(assessmentId).forEach { att ->
            if (att.uploadState != AttachmentUploadState.Uploaded.name) {
                attachmentDao.updateUploadState(att.id, AttachmentUploadState.Uploaded.name)
            }
        }
    }
}
