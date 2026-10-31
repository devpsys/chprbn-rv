package ng.com.chprbn.mobile.feature.accreditation.domain.model

data class AssessmentDraftRecord(
    val id: String,
    val institutionId: String,
    val institutionName: String,
    val cycleLabel: String,
    val facilityCode: String,
    val locationLabel: String,
    val status: DraftPersistenceStatus,
    val updatedAt: Long,
)

data class SsqSectionSummary(
    val key: SsqSectionKey,
    val codeLabel: String,
    val rangeLabel: String,
    val title: String,
    val subtitle: String,
    val status: DraftPersistenceStatus,
    val itemsCompleted: Int,
    val totalItems: Int,
)

data class AssessmentOverview(
    val assessmentId: String,
    val institutionId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val cycleLabel: String,
    val sections: List<SsqSectionSummary>,
)

data class SsqSectionDraftRecord(
    val assessmentId: String,
    val sectionKey: SsqSectionKey,
    val codeLabel: String,
    val rangeLabel: String,
    val title: String,
    val subtitle: String,
    val status: DraftPersistenceStatus,
    val payloadJson: String,
    val itemsCompleted: Int,
    val totalItems: Int,
)

data class CriteriaDraftRecord(
    val assessmentId: String,
    val payloadJson: String,
    val evaluatedCount: Int,
    val totalCriteria: Int,
    val status: DraftPersistenceStatus,
)

data class ScoreDraftRecord(
    val assessmentId: String,
    val payloadJson: String,
    val completedCount: Int,
    val totalGroups: Int,
    val status: DraftPersistenceStatus,
)

data class AccreditationAttachmentRecord(
    val id: String,
    val assessmentId: String,
    val parentKey: String,
    val title: String,
    val sizeLabel: String,
    val isPhoto: Boolean,
    val localPath: String,
    val contentUri: String?,
    val locationLabel: String?,
    val uploadState: AttachmentUploadState,
)

data class AssessmentDecision(
    val decisionId: String,
    val institutionId: String,
    val institutionName: String,
    val locationLabel: String,
    val facilityCode: String,
    val assessmentDateLabel: String,
    val gradeLabel: String,
    val decisionLabel: String,
    val accreditationPeriod: String,
    val overallScorePercentLabel: String,
    val studentQuotaLabel: String,
    val receiptRef: String,
    val rawScoreRowsJson: String,
    val conditionsJson: String,
)

sealed interface DraftOpResult {
    data object Success : DraftOpResult
    data class Error(val message: String) : DraftOpResult
}

sealed interface GetOverviewResult {
    data class Success(val overview: AssessmentOverview) : GetOverviewResult
    data class Error(val message: String) : GetOverviewResult
}

sealed interface SubmitAssessmentResult {
    data class Success(val decisionId: String) : SubmitAssessmentResult
    data class Error(val message: String) : SubmitAssessmentResult
}

sealed interface GetDecisionResult {
    data class Success(val decision: AssessmentDecision) : GetDecisionResult
    data class NotFound(val decisionId: String) : GetDecisionResult
    data class Error(val message: String) : GetDecisionResult
}
