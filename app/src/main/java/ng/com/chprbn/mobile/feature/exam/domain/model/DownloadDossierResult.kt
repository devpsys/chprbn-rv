package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Outcome of the day-dossier download for the officer's centre. Carries
 * counters on success so the loading screen can surface meaningful
 * progress once the operation completes.
 */
sealed interface DownloadDossierResult {
    data class Success(
        val papersCount: Int,
        val candidatesCount: Int,
    ) : DownloadDossierResult

    data class Error(val message: String) : DownloadDossierResult
}
