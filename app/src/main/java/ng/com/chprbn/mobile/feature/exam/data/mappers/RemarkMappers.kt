package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark

internal fun RemarkEntity.toDomain(): Remark = Remark(
    id = id,
    candidateId = candidateId,
    paperId = paperId,
    body = body,
    severity = severity.toRemarkSeverity(),
    createdAt = createdAt,
    syncStatus = syncStatus.toSyncStatus(),
    syncError = syncError,
)

internal fun Remark.toEntity(): RemarkEntity = RemarkEntity(
    id = id,
    candidateId = candidateId,
    paperId = paperId,
    body = body,
    severity = severity.toDbValue(),
    createdAt = createdAt,
    syncStatus = syncStatus.toDbValue(),
    syncError = syncError,
    lastSyncAttemptAt = null,
)
