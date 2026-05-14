package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion

/**
 * Per-candidate practical scoring. Reads the section summary for the
 * `AssessmentPracticalSections` hub, the per-question scoring shape for the
 * `AssessmentPracticalScoring` screen, and accepts individual score upserts
 * (one per stepper tap) plus a section-level commit that enqueues sync for
 * every pending row in the section.
 *
 * `getQuestions` returns each question alongside its current local score
 * (`null` when the candidate hasn't scored that question yet) so the screen
 * can render in one pass without a second query for scores.
 */
interface PracticalScoringRepository {

    suspend fun getSections(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalSectionSummary>

    suspend fun getQuestions(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): List<Pair<SectionQuestion, PracticalScore?>>

    suspend fun recordScore(score: PracticalScore): SaveResult

    suspend fun commitSection(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): SaveResult
}
