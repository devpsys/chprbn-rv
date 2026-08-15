package ng.com.chprbn.mobile.feature.assessment.data.source

/**
 * Tries the [primary] source (the live API) and falls back to [fallback]
 * (the in-memory fake) **only** when the primary returned successfully
 * but with no data. On a transport / envelope error the primary throws —
 * the exception propagates to the repository so a real error is surfaced
 * to the officer instead of a Fake package indistinguishable from live
 * data (A-S1 audit finding).
 *
 * Wired only in debug builds (see `AssessmentDataModule.provideAssessmentPackageRemoteSource`);
 * release builds bind the API source directly.
 */
class CompositeAssessmentPackageRemoteSource(
    private val primary: AssessmentPackageRemoteSource,
    private val fallback: AssessmentPackageRemoteSource,
) : AssessmentPackageRemoteSource {

    override suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle? =
        primary.fetchPackage(scheduleId) ?: fallback.fetchPackage(scheduleId)
}
