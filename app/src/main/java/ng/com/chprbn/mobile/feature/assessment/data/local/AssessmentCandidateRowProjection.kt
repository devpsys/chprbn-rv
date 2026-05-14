package ng.com.chprbn.mobile.feature.assessment.data.local

/**
 * Projection backing `AssessmentCandidateDao.rowsForSchedule`. Mirrors the
 * domain `AssessmentCandidateRow` shape so mapping is a flat field copy,
 * but stays in the data layer because `syncStatus` arrives as a String
 * (the SQL `CASE` produces a literal).
 */
data class AssessmentCandidateRowProjection(
    val candidateId: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String?,
    val aggregateScore: Int,
    val scoredQuestions: Int,
    val totalQuestions: Int,
    val syncStatus: String,
)
