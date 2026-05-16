package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore

/**
 * Domain → batch-item DTO for the sync engine. Matches verification's
 * `SyncPayloadMappers.kt` layout: one extension per outbound payload, all
 * in one file because the file is small and the related functions are
 * easier to find together.
 *
 * No `syncStatus` / `syncError` cross the wire — those are mobile-only
 * bookkeeping written to local rows after a server response arrives.
 * `clientId` is synthesised from the row's composite identity so retries
 * carry the same id.
 */
internal fun PracticalScore.toSyncItemDto(): PracticalScoreSyncItemDto =
    PracticalScoreSyncItemDto(
        clientId = practicalScoreClientId(scheduleId, candidateId, questionId),
        scheduleId = scheduleId,
        candidateId = candidateId,
        questionId = questionId,
        score = score,
        scoredAt = scoredAt,
    )

internal fun ProjectScore.toSyncItemDto(): ProjectScoreSyncItemDto =
    ProjectScoreSyncItemDto(
        clientId = projectScoreClientId(scheduleId, candidateId),
        scheduleId = scheduleId,
        candidateId = candidateId,
        score = score,
        maxScore = maxScore,
        scoredAt = scoredAt,
    )

/** Stable client-side correlation key for a practical-score row. */
internal fun practicalScoreClientId(
    scheduleId: String,
    candidateId: String,
    questionId: String,
): String = "$scheduleId:$candidateId:$questionId"

/** Stable client-side correlation key for a project-score row. */
internal fun projectScoreClientId(scheduleId: String, candidateId: String): String =
    "$scheduleId:$candidateId"
