package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.ConfirmationDialog
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog
import ng.com.chprbn.mobile.core.designsystem.components.SyncingOverlay

private val InfoBlue = Color(0xFF0277BD)

@Composable
fun LocalDraftsContent(
    modifier: Modifier = Modifier,
    uiState: LocalDraftsUiState,
    onBack: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onFilterSelected: (LocalDraftFilter) -> Unit = {},
    onDraftSelectionToggled: (draftId: String, selected: Boolean) -> Unit = { _, _ -> },
    onResumeDraft: (LocalDraftItemUi) -> Unit = {},
    onSyncAllDrafts: () -> Unit = {},
    onDiscardSelectedClicked: () -> Unit = {},
    onConfirmDiscard: () -> Unit = {},
    onDismissDiscard: () -> Unit = {},
    onDismissSyncSuccess: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AssessmentFlowTopBar(
                    title = stringResource(R.string.accreditation_drafts_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                if (!uiState.isLoading) {
                    LocalDraftsBottomBar(
                        selectedCount = uiState.selectedCount,
                        syncEnabled = uiState.drafts.any {
                            it.syncState == LocalDraftSyncState.PendingSync ||
                                it.syncState == LocalDraftSyncState.SyncFailed
                        } && !uiState.isSyncing,
                        discardEnabled = uiState.selectedCount > 0 && !uiState.isSyncing,
                        onSyncAll = onSyncAllDrafts,
                        onDiscardSelected = onDiscardSelectedClicked,
                    )
                }
            },
        ) { paddingValues ->
            when {
                uiState.isLoading -> LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    item {
                        LocalDraftsSearchField(
                            query = uiState.searchQuery,
                            onQueryChanged = onSearchQueryChanged,
                        )
                    }
                    item {
                        LocalDraftsFilterChips(
                            selected = uiState.selectedFilter,
                            onSelected = onFilterSelected,
                        )
                    }
                    item { LocalDraftsVaultNote() }
                    if (uiState.visibleDrafts.isEmpty()) {
                        item { LocalDraftsEmptyState() }
                    } else {
                        items(uiState.visibleDrafts, key = { it.id }) { draft ->
                            LocalDraftRow(
                                draft = draft,
                                onSelectionChanged = { selected ->
                                    onDraftSelectionToggled(draft.id, selected)
                                },
                                onResume = { onResumeDraft(draft) },
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        if (uiState.isSyncing) {
            SyncingOverlay(
                title = stringResource(R.string.accreditation_drafts_sync_overlay_title),
                subtitle = stringResource(R.string.accreditation_drafts_sync_overlay_subtitle),
                encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
            )
        }

        if (uiState.showDiscardConfirm) {
            ConfirmationDialog(
                title = stringResource(R.string.accreditation_drafts_discard_title),
                message = stringResource(R.string.accreditation_drafts_discard_message),
                primaryButtonText = stringResource(R.string.accreditation_drafts_discard_confirm),
                secondaryButtonText = stringResource(R.string.accreditation_drafts_discard_keep),
                onConfirm = onConfirmDiscard,
                onCancel = onDismissDiscard,
                onDismiss = onDismissDiscard,
            )
        }

        if (uiState.showSyncSuccess) {
            SuccessDialog(
                title = stringResource(R.string.accreditation_drafts_sync_success_title),
                message = stringResource(
                    R.string.accreditation_drafts_sync_success_message_format,
                    uiState.lastSyncedCount,
                ),
                primaryButtonText = stringResource(R.string.accreditation_drafts_sync_success_done),
                onPrimary = onDismissSyncSuccess,
                onDismiss = onDismissSyncSuccess,
            )
        }
    }
}

@Composable
private fun LocalDraftsSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.accreditation_drafts_search_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = PrimaryGreen,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            cursorColor = PrimaryGreen,
        ),
    )
}

@Composable
private fun LocalDraftsFilterChips(
    selected: LocalDraftFilter,
    onSelected: (LocalDraftFilter) -> Unit,
) {
    val filters = listOf(
        LocalDraftFilter.All to R.string.accreditation_drafts_filter_all,
        LocalDraftFilter.PendingSync to R.string.accreditation_drafts_filter_pending,
        LocalDraftFilter.Synced to R.string.accreditation_drafts_filter_synced,
        LocalDraftFilter.SyncFailed to R.string.accreditation_drafts_filter_failed,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { (filter, labelRes) ->
            val isSelected = selected == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(filter) },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
private fun LocalDraftsVaultNote() {
    Surface(
        color = PrimaryGreen.copy(alpha = 0.10f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.accreditation_drafts_vault_note),
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryGreen,
            )
        }
    }
}

@Composable
private fun LocalDraftsEmptyState() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(54.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.accreditation_drafts_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.accreditation_drafts_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LocalDraftRow(
    draft: LocalDraftItemUi,
    onSelectionChanged: (Boolean) -> Unit,
    onResume: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (draft.isSelected) {
            PrimaryGreen.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (draft.isSelected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onResume)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = draft.isSelected,
                onCheckedChange = onSelectionChanged,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = draft.assignmentCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    LocalDraftSyncBadge(state = draft.syncState)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = draft.institutionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(
                                R.string.accreditation_drafts_sections_format,
                                draft.sectionsCompleted,
                                draft.totalSections,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(
                                R.string.accreditation_drafts_attachments_format,
                                draft.attachmentsCount,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.accreditation_drafts_modified_format,
                        draft.lastModifiedLabel,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.accreditation_drafts_resume_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun LocalDraftSyncBadge(
    state: LocalDraftSyncState,
    modifier: Modifier = Modifier,
) {
    val (labelRes, tint, bg) = when (state) {
        LocalDraftSyncState.PendingSync -> Triple(
            R.string.accreditation_drafts_status_pending,
            WarningYellow,
            WarningYellow.copy(alpha = 0.14f),
        )
        LocalDraftSyncState.Syncing -> Triple(
            R.string.accreditation_drafts_status_syncing,
            InfoBlue,
            InfoBlue.copy(alpha = 0.14f),
        )
        LocalDraftSyncState.Synced -> Triple(
            R.string.accreditation_drafts_status_synced,
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.14f),
        )
        LocalDraftSyncState.SyncFailed -> Triple(
            R.string.accreditation_drafts_status_failed,
            ErrorRed,
            ErrorRed.copy(alpha = 0.14f),
        )
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LocalDraftsBottomBar(
    selectedCount: Int,
    syncEnabled: Boolean,
    discardEnabled: Boolean,
    onSyncAll: () -> Unit,
    onDiscardSelected: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.accreditation_drafts_sync_all),
                onClick = onSyncAll,
                enabled = syncEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = onDiscardSelected,
                enabled = discardEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (discardEnabled) ErrorRed else ErrorRed.copy(alpha = 0.3f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = if (discardEnabled) ErrorRed else ErrorRed.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedCount > 0) {
                        stringResource(
                            R.string.accreditation_drafts_discard_selected_count_format,
                            selectedCount,
                        )
                    } else {
                        stringResource(R.string.accreditation_drafts_discard_selected)
                    },
                    color = if (discardEnabled) ErrorRed else ErrorRed.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocalDraftsContentPreview() {
    ChprbnTheme {
        LocalDraftsContent(uiState = LocalDraftsUiState.placeholder())
    }
}

@Preview(showBackground = true)
@Composable
private fun LocalDraftsEmptyPreview() {
    ChprbnTheme {
        LocalDraftsContent(
            uiState = LocalDraftsUiState.placeholder().copy(
                drafts = emptyList(),
                visibleDrafts = emptyList(),
            ),
        )
    }
}
