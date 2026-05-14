package ng.com.chprbn.mobile.feature.exam.data.source

/**
 * Tries the [primary] source (the live API) and falls back to
 * [fallback] (the in-memory fake) when the primary returns `null`.
 * Mirrors verification's `CompositeLicenseRecordRemoteSource` pattern.
 *
 * Production wiring binds `primary = ApiExamDossierRemoteSource` and
 * `fallback = FakeExamDossierRemoteSource` so the screens stay
 * functional before the backend ships.
 */
class CompositeExamDossierRemoteSource(
    private val primary: ExamDossierRemoteSource,
    private val fallback: ExamDossierRemoteSource,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? =
        primary.fetchDossier() ?: fallback.fetchDossier()
}
