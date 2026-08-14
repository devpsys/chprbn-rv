package ng.com.chprbn.mobile.feature.exam.data.source

import android.util.Log

/**
 * Tries the [primary] source (the live API) and falls back to
 * [fallback] (the in-memory fake) **only** when the primary returned
 * successfully but with no data. On a transport / envelope error the
 * primary throws — that exception is left to propagate so the repository
 * can surface a real error to the user instead of a Fake dossier
 * indistinguishable from live data (E1 audit finding).
 *
 * **Currently unwired.** Previously bound in debug builds only (see
 * `ExamDataModule.provideExamDossierRemoteSource`), but that made a
 * live-but-center-less API response silently indistinguishable from a
 * real download whenever a debug build was sideloaded onto a field
 * device — that's what happened when an officer saw a fake 3-paper /
 * 3-candidate dossier reported as a real download. `ExamDataModule` now
 * binds [ApiExamDossierRemoteSource] directly in every build type; this
 * class is kept only for its own unit test / potential future dev-mode
 * reactivation behind an explicit, non-signing-config-tied flag. The
 * [Log.w] below would be the only trace left if it's ever re-wired.
 */
class CompositeExamDossierRemoteSource(
    private val primary: ExamDossierRemoteSource,
    private val fallback: ExamDossierRemoteSource,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? {
        val primaryResult = primary.fetchDossier()
        if (primaryResult != null) return primaryResult
        Log.w(
            TAG,
            "Primary (live API) dossier source returned null — falling back to " +
                "FakeExamDossierRemoteSource. Whatever gets persisted next is NOT real data.",
        )
        return fallback.fetchDossier()
    }

    private companion object {
        const val TAG = "ExamDossier"
    }
}
