package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule

/**
 * Tries the [primary] source (the live API) and falls back to [fallback]
 * (the in-memory fake) when the primary returns `null`. Mirrors
 * verification's `CompositeLicenseRecordRemoteSource` pattern.
 *
 * Production wiring binds `primary = ApiAssessmentPackageRemoteSource` and
 * `fallback = FakeAssessmentPackageRemoteSource` so the screens stay
 * functional before the backend ships. Once the live API is reliable, the
 * fallback can be swapped out by changing one Hilt binding.
 */
class CompositeAssessmentPackageRemoteSource(
    private val primary: AssessmentPackageRemoteSource,
    private val fallback: AssessmentPackageRemoteSource,
) : AssessmentPackageRemoteSource {

    override suspend fun fetchSchedules(): List<AssessmentSchedule>? =
        primary.fetchSchedules() ?: fallback.fetchSchedules()

    override suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle? =
        primary.fetchPackage(scheduleId) ?: fallback.fetchPackage(scheduleId)
}
