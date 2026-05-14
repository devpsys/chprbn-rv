package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Coarse aggregate-score band used to colour the score pill on the
 * `AssessmentCandidates` screen. The numeric threshold separating `Normal`
 * from `Low` lives in [GetAssessmentCandidatesUseCase] — kept out of the
 * model so different cohorts can adopt different thresholds without a
 * schema change.
 */
enum class ScoreLevel { Normal, Low }
