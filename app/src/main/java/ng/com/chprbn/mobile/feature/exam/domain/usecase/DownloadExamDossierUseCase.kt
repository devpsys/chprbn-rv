package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import javax.inject.Inject

/**
 * Pulls the day's dossier (papers + candidates + assignments) into the
 * local encrypted cache. Destructive — wipes existing reference rows
 * first; attendance / remark writes are NOT touched (the survival
 * contract behind the download-warning prompt).
 */
class DownloadExamDossierUseCase @Inject constructor(
    private val repository: ExamSyncRepository,
) {
    suspend operator fun invoke(): DownloadDossierResult = repository.downloadDossier()
}
