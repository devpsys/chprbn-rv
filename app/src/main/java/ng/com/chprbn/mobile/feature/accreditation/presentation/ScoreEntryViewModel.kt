package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ScoreEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<ScoreEntryUiState>(
        if (institutionId.isBlank()) {
            ScoreEntryUiState.Error("Missing institution")
        } else {
            ScoreEntryUiState.placeholder(institutionId)
        },
    )
    val uiState: StateFlow<ScoreEntryUiState> = _uiState.asStateFlow()

    fun onRawScoreChange(group: ScoreCriterionGroup, value: String) =
        updateRow(group) { it.copy(rawScore = digitsOnly(value)) }

    fun onObservationsChange(group: ScoreCriterionGroup, value: String) =
        updateRow(group) { it.copy(observations = value) }

    fun onAttachmentAdded(group: ScoreCriterionGroup, contentUri: String, isPhoto: Boolean) =
        updateRow(group) { row ->
            row.copy(
                attachments = row.attachments + SsqAttachmentUiState(
                    id = "att-${UUID.randomUUID()}",
                    title = filenameFromContentUri(contentUri),
                    sizeLabel = "Local",
                    isPhoto = isPhoto,
                    contentUri = contentUri,
                ),
            )
        }

    fun onRemoveAttachment(group: ScoreCriterionGroup, attachmentId: String) =
        updateRow(group) { row ->
            row.copy(attachments = row.attachments.filterNot { it.id == attachmentId })
        }

    private fun filenameFromContentUri(contentUri: String): String {
        val segment = Uri.parse(contentUri).lastPathSegment.orEmpty()
        return segment.substringAfterLast('/').takeIf { it.isNotBlank() } ?: "attachment.jpg"
    }

    fun onSaveDraft() {
        updateSuccess { it.copy(showSavedDialog = true) }
    }

    fun dismissSavedDialog() {
        updateSuccess { it.copy(showSavedDialog = false) }
    }

    private fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    private fun updateRow(
        group: ScoreCriterionGroup,
        transform: (ScoreEntryRowUiState) -> ScoreEntryRowUiState,
    ) {
        updateSuccess { data ->
            data.copy(
                rows = data.rows.map { row ->
                    if (row.group == group) transform(row) else row
                },
            )
        }
    }

    private fun updateSuccess(transform: (ScoreEntryData) -> ScoreEntryData) {
        _uiState.update { state ->
            val success = state as? ScoreEntryUiState.Success ?: return@update state
            ScoreEntryUiState.Success(transform(success.data))
        }
    }
}
