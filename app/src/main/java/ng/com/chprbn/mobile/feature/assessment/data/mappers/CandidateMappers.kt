package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentCandidateDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ScheduleCandidateAssignmentDto
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateRowProjection
import ng.com.chprbn.mobile.feature.assessment.data.local.ScheduleCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel

internal fun AssessmentCandidateEntity.toDomain(): Candidate = Candidate(
    id = id,
    examNumber = examNumber,
    fullName = fullName,
    photoUrl = photoUrl,
)

internal fun Candidate.toAssessmentCandidateEntity(): AssessmentCandidateEntity =
    AssessmentCandidateEntity(
        id = id,
        examNumber = examNumber,
        fullName = fullName,
        photoUrl = photoUrl,
    )

/**
 * Folds a [AssessmentCandidateRowProjection] (raw SQL result) into the
 * domain [AssessmentCandidateRow], deriving [ScoreLevel] via the default
 * threshold. Use cases that need a different threshold can post-process
 * with `.copy(level = ScoreLevel.fromScore(score, customThreshold))`.
 */
internal fun AssessmentCandidateRowProjection.toDomain(
    threshold: Int = ScoreLevel.DEFAULT_LOW_THRESHOLD,
): AssessmentCandidateRow = AssessmentCandidateRow(
    candidate = Candidate(
        id = candidateId,
        examNumber = examNumber,
        fullName = fullName,
        photoUrl = photoUrl,
    ),
    aggregateScore = aggregateScore,
    level = ScoreLevel.fromScore(aggregateScore, threshold),
    scoredQuestions = scoredQuestions,
    totalQuestions = totalQuestions,
    syncStatus = syncStatus.toSyncStatus(),
)

internal fun AssessmentCandidateDto.toEntity(): AssessmentCandidateEntity? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    return AssessmentCandidateEntity(
        id = safeId,
        examNumber = examNumber.orEmpty(),
        fullName = fullName.orEmpty(),
        photoUrl = photoUrl,
    )
}

internal fun ScheduleCandidateAssignmentDto.toEntity(): ScheduleCandidateAssignmentEntity? {
    val safeScheduleId = scheduleId?.takeIf { it.isNotBlank() } ?: return null
    val safeCandidateId = candidateId?.takeIf { it.isNotBlank() } ?: return null
    return ScheduleCandidateAssignmentEntity(
        scheduleId = safeScheduleId,
        candidateId = safeCandidateId,
    )
}
