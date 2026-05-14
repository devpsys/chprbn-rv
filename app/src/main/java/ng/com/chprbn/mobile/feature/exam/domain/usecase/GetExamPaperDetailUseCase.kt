package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import javax.inject.Inject

class GetExamPaperDetailUseCase @Inject constructor(
    private val repository: ExamPaperRepository,
) {
    suspend operator fun invoke(paperId: String): ExamPaperDetailResult {
        val trimmed = paperId.trim()
        if (trimmed.isEmpty()) {
            return ExamPaperDetailResult.Error("Paper id is required.")
        }
        return repository.getPaperDetail(trimmed)
    }
}
