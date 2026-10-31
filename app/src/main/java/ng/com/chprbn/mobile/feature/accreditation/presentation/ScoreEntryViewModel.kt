package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.AddAccreditationAttachmentUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetScoreEntryDraftUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.RemoveAccreditationAttachmentUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.SaveScoreEntryDraftUseCase

@HiltViewModel
class ScoreEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getScoreDraft: GetScoreEntryDraftUseCase,
    private val saveScoreDraft: SaveScoreEntryDraftUseCase,
    private val addAttachment: AddAccreditationAttachmentUseCase,
    private val removeAttachment: RemoveAccreditationAttachmentUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<ScoreEntryUiState>(
        if (institutionId.isBlank()) {
            ScoreEntryUiState.Error("Missing institution")
        } else {
            ScoreEntryUiState.Loading
        },
    )
    val uiState: StateFlow<ScoreEntryUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch { load() }
        }
    }

    private suspend fun load() {
        val record = getScoreDraft(institutionId)
        val fromJson = parseScoreEntryData(record?.payloadJson)
        val base = fromJson ?: ScoreEntryUiState.placeholder(institutionId).data
        _uiState.value = ScoreEntryUiState.Success(
            base.copy(institutionId = institutionId, showSavedDialog = false),
        )
    }

    fun onRawScoreChange(group: ScoreCriterionGroup, value: String) =
        updateRow(group) { it.copy(rawScore = digitsOnly(value)) }

    fun onObservationsChange(group: ScoreCriterionGroup, value: String) =
        updateRow(group) { it.copy(observations = value) }

    fun onAttachmentAdded(group: ScoreCriterionGroup, contentUri: String, isPhoto: Boolean) {
        viewModelScope.launch {
            val stored = addAttachment(
                institutionId = institutionId,
                parentKey = "score:${group.name}",
                fromUri = Uri.parse(contentUri),
                isPhoto = isPhoto,
            )
            updateRow(group) { row ->
                row.copy(
                    attachments = row.attachments + SsqAttachmentUiState(
                        id = stored?.id ?: "att-${UUID.randomUUID()}",
                        title = stored?.title ?: filenameFromContentUri(contentUri),
                        sizeLabel = stored?.sizeLabel ?: "Local",
                        isPhoto = isPhoto,
                        contentUri = stored?.contentUri ?: contentUri,
                    ),
                )
            }
        }
    }

    fun onRemoveAttachment(group: ScoreCriterionGroup, attachmentId: String) {
        viewModelScope.launch {
            removeAttachment(attachmentId)
            updateRow(group) { row ->
                row.copy(attachments = row.attachments.filterNot { it.id == attachmentId })
            }
        }
    }

    private fun filenameFromContentUri(contentUri: String): String {
        val segment = Uri.parse(contentUri).lastPathSegment.orEmpty()
        return segment.substringAfterLast('/').takeIf { it.isNotBlank() } ?: "attachment.jpg"
    }

    fun onSaveDraft() {
        val success = _uiState.value as? ScoreEntryUiState.Success ?: return
        val data = success.data.copy(showSavedDialog = true)
        _uiState.value = ScoreEntryUiState.Success(data)
        viewModelScope.launch {
            saveScoreDraft(
                institutionId = institutionId,
                payloadJson = data.copy(showSavedDialog = false).toPayloadJson(),
                completedCount = data.completedCount,
                totalGroups = data.totalGroups,
            )
        }
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
