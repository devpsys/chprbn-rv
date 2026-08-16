package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Outcome of the day-dossier download for the officer's centre. Carries
 * counters on success so the loading screen can surface meaningful
 * progress once the operation completes.
 *
 * Merge semantics: candidates are additive (new rows added, already-known
 * ones skipped), so [newCandidatesCount] is how many actually landed and
 * [skippedCandidatesCount] is how many the wire re-sent that were already
 * cached. [candidatesCount] is the total in the wire payload —
 * `newCandidatesCount + skippedCandidatesCount`, kept for callers that
 * only care about the response size. Papers are authoritative
 * (wipe-and-replace), so [papersCount] is both the total and effectively
 * the "after" count.
 */
sealed interface DownloadDossierResult {
    data class Success(
        val papersCount: Int,
        val candidatesCount: Int,
        val newCandidatesCount: Int,
        val skippedCandidatesCount: Int,
    ) : DownloadDossierResult

    data class Error(val message: String) : DownloadDossierResult
}
