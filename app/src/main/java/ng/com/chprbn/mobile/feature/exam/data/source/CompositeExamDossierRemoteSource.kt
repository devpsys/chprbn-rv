package ng.com.chprbn.mobile.feature.exam.data.source

/**
 * Tries the [primary] source (the live API) and falls back to
 * [fallback] (the in-memory fake) **only** when the primary returned
 * successfully but with no data. On a transport / envelope error the
 * primary throws — that exception is left to propagate so the repository
 * can surface a real error to the user instead of a Fake dossier
 * indistinguishable from live data (E1 audit finding).
 *
 * Wired only in debug builds (see `ExamDataModule.provideExamDossierRemoteSource`);
 * release builds bind the API source directly.
 */
class CompositeExamDossierRemoteSource(
    private val primary: ExamDossierRemoteSource,
    private val fallback: ExamDossierRemoteSource,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? =
        primary.fetchDossier() ?: fallback.fetchDossier()
}
