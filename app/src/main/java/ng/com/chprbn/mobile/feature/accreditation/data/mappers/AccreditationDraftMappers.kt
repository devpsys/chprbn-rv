package ng.com.chprbn.mobile.feature.accreditation.data.mappers

import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentEntity
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftEntity
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationAttachmentRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AttachmentUploadState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftSyncState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun AssessmentDraftRecord.toEntity(): AssessmentDraftEntity = AssessmentDraftEntity(
    id = id,
    institutionId = institutionId,
    institutionName = institutionName,
    cycleLabel = cycleLabel,
    facilityCode = facilityCode,
    locationLabel = locationLabel,
    status = status.name,
    updatedAt = updatedAt,
)

fun AssessmentDraftEntity.toDomain(): AssessmentDraftRecord = AssessmentDraftRecord(
    id = id,
    institutionId = institutionId,
    institutionName = institutionName,
    cycleLabel = cycleLabel,
    facilityCode = facilityCode,
    locationLabel = locationLabel,
    status = status.toDraftPersistenceStatus(),
    updatedAt = updatedAt,
)

fun SsqSectionDraftRecord.toEntity(): SsqSectionDraftEntity = SsqSectionDraftEntity(
    assessmentId = assessmentId,
    sectionKey = sectionKey.key,
    codeLabel = codeLabel,
    rangeLabel = rangeLabel,
    title = title,
    subtitle = subtitle,
    status = status.name,
    payloadJson = payloadJson,
    itemsCompleted = itemsCompleted,
    totalItems = totalItems,
)

fun SsqSectionDraftEntity.toDomain(): SsqSectionDraftRecord = SsqSectionDraftRecord(
    assessmentId = assessmentId,
    sectionKey = SsqSectionKey.fromKey(sectionKey),
    codeLabel = codeLabel,
    rangeLabel = rangeLabel,
    title = title,
    subtitle = subtitle,
    status = status.toDraftPersistenceStatus(),
    payloadJson = payloadJson,
    itemsCompleted = itemsCompleted,
    totalItems = totalItems,
)

fun SsqSectionDraftEntity.toSummary(): SsqSectionSummary = SsqSectionSummary(
    key = SsqSectionKey.fromKey(sectionKey),
    codeLabel = codeLabel,
    rangeLabel = rangeLabel,
    title = title,
    subtitle = subtitle,
    status = status.toDraftPersistenceStatus(),
    itemsCompleted = itemsCompleted,
    totalItems = totalItems,
)

fun CriteriaDraftRecord.toEntity(): CriteriaDraftEntity = CriteriaDraftEntity(
    assessmentId = assessmentId,
    payloadJson = payloadJson,
    evaluatedCount = evaluatedCount,
    totalCriteria = totalCriteria,
    status = status.name,
)

fun CriteriaDraftEntity.toDomain(): CriteriaDraftRecord = CriteriaDraftRecord(
    assessmentId = assessmentId,
    payloadJson = payloadJson,
    evaluatedCount = evaluatedCount,
    totalCriteria = totalCriteria,
    status = status.toDraftPersistenceStatus(),
)

fun ScoreDraftRecord.toEntity(): ScoreDraftEntity = ScoreDraftEntity(
    assessmentId = assessmentId,
    payloadJson = payloadJson,
    completedCount = completedCount,
    totalGroups = totalGroups,
    status = status.name,
)

fun ScoreDraftEntity.toDomain(): ScoreDraftRecord = ScoreDraftRecord(
    assessmentId = assessmentId,
    payloadJson = payloadJson,
    completedCount = completedCount,
    totalGroups = totalGroups,
    status = status.toDraftPersistenceStatus(),
)

fun AccreditationAttachmentRecord.toEntity(): AccreditationAttachmentEntity =
    AccreditationAttachmentEntity(
        id = id,
        assessmentId = assessmentId,
        parentKey = parentKey,
        title = title,
        sizeLabel = sizeLabel,
        isPhoto = isPhoto,
        localPath = localPath,
        contentUri = contentUri,
        locationLabel = locationLabel,
        uploadState = uploadState.name,
    )

fun AccreditationAttachmentEntity.toDomain(): AccreditationAttachmentRecord =
    AccreditationAttachmentRecord(
        id = id,
        assessmentId = assessmentId,
        parentKey = parentKey,
        title = title,
        sizeLabel = sizeLabel,
        isPhoto = isPhoto,
        localPath = localPath,
        contentUri = contentUri,
        locationLabel = locationLabel,
        uploadState = uploadState.toAttachmentUploadState(),
    )

fun AssessmentDraftEntity.toLocalDraftListItem(
    sectionsCompleted: Int,
    attachmentsCount: Int,
    attemptCount: Int = 0,
    lastRawError: String? = null,
): LocalDraftListItem = LocalDraftListItem(
    id = id,
    institutionId = institutionId,
    institutionName = institutionName,
    assignmentCode = facilityCode,
    sectionsCompleted = sectionsCompleted,
    totalSections = 6,
    attachmentsCount = attachmentsCount,
    lastModifiedLabel = formatUpdatedAt(updatedAt),
    syncState = status.toLocalDraftSyncState(),
    attemptCount = attemptCount,
    lastRawError = lastRawError,
)

fun String.toDraftPersistenceStatus(): DraftPersistenceStatus =
    runCatching { DraftPersistenceStatus.valueOf(this) }
        .getOrDefault(DraftPersistenceStatus.NotStarted)

fun String.toAttachmentUploadState(): AttachmentUploadState =
    runCatching { AttachmentUploadState.valueOf(this) }
        .getOrDefault(AttachmentUploadState.LocalOnly)

fun String.toLocalDraftSyncState(): LocalDraftSyncState = when (toDraftPersistenceStatus()) {
    DraftPersistenceStatus.PendingSync,
    DraftPersistenceStatus.SavedLocally,
    DraftPersistenceStatus.InProgress,
    -> LocalDraftSyncState.PendingSync
    DraftPersistenceStatus.Synced -> LocalDraftSyncState.Synced
    DraftPersistenceStatus.SyncFailed -> LocalDraftSyncState.SyncFailed
    DraftPersistenceStatus.NotStarted -> LocalDraftSyncState.PendingSync
}

private fun formatUpdatedAt(millis: Long): String {
    val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    return fmt.format(Date(millis))
}
