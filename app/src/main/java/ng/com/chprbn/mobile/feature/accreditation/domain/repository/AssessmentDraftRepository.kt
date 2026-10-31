package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentOverview
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetOverviewResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey

interface AssessmentDraftRepository {
    suspend fun getOrCreateCurrent(institutionId: String): AssessmentDraftRecord

    suspend fun getOverview(institutionId: String): GetOverviewResult

    suspend fun getSection(
        assessmentId: String,
        sectionKey: SsqSectionKey,
    ): SsqSectionDraftRecord?

    suspend fun saveSection(
        assessmentId: String,
        sectionKey: SsqSectionKey,
        payloadJson: String,
        status: DraftPersistenceStatus,
        itemsCompleted: Int,
        totalItems: Int,
    ): DraftOpResult

    suspend fun getCriteria(assessmentId: String): CriteriaDraftRecord?

    suspend fun saveCriteria(
        assessmentId: String,
        payloadJson: String,
        evaluatedCount: Int,
        totalCriteria: Int,
        status: DraftPersistenceStatus,
    ): DraftOpResult

    suspend fun getScores(assessmentId: String): ScoreDraftRecord?

    suspend fun saveScores(
        assessmentId: String,
        payloadJson: String,
        completedCount: Int,
        totalGroups: Int,
        status: DraftPersistenceStatus,
    ): DraftOpResult

    suspend fun saveAllSections(assessmentId: String): DraftOpResult

    suspend fun discard(assessmentIds: List<String>): DraftOpResult

    suspend fun markSynced(assessmentId: String): DraftOpResult

    suspend fun getDraft(assessmentId: String): AssessmentDraftRecord?
}
