package ng.com.chprbn.mobile.feature.assessment.data.repository

import ng.com.chprbn.mobile.core.session.SessionScopedCleaner
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import javax.inject.Inject

/**
 * SessionScopedCleaner wrapper around
 * [AssessmentScheduleRepository.clearCache] with `scheduleId = null`
 * (global wipe — also drops scores, see the impl comment). Bound via
 * `@IntoSet` in `AssessmentDataModule`; the core [SessionCleaner]
 * invokes it on logout (A2 audit fix).
 */
class AssessmentSessionCleaner @Inject constructor(
    private val assessmentScheduleRepository: AssessmentScheduleRepository,
) : SessionScopedCleaner {
    override suspend fun clear() {
        assessmentScheduleRepository.clearCache(scheduleId = null)
    }
}
