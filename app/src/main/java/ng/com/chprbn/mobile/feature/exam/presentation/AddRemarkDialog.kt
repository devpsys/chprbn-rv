package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/** Groups [RemarkType] entries under the dialog's section headers, in display order. */
enum class RemarkSection(@StringRes val labelRes: Int) {
    Attendance(R.string.exam_candidates_remark_section_attendance),
    Status(R.string.exam_candidates_remark_section_status),
    Conduct(R.string.exam_candidates_remark_section_conduct),
}

/**
 * Fixed set of CHPRBN remark codes the officer picks from — not a
 * free-text field. [labelRes] becomes the persisted [ng.com.chprbn.mobile.feature.exam.domain.model.Remark.body]
 * (see `ExamCandidatesViewModel.onSaveRemark`); [severity] drives the
 * accent color/icon here and the row's future severity styling.
 */
enum class RemarkType(
    val code: String,
    @StringRes val labelRes: Int,
    val section: RemarkSection,
    val severity: RemarkSeverity,
    val icon: ImageVector,
) {
    Absenteeism(
        code = "ABS",
        labelRes = R.string.exam_candidates_remark_type_absenteeism,
        section = RemarkSection.Attendance,
        severity = RemarkSeverity.Info,
        icon = Icons.Filled.CheckCircle,
    ),
    AbsentWithExcuse(
        code = "AE",
        labelRes = R.string.exam_candidates_remark_type_absent_with_excuse,
        section = RemarkSection.Attendance,
        severity = RemarkSeverity.Warning,
        icon = Icons.Filled.Warning,
    ),
    AbsentWithoutExcuse(
        code = "AW",
        labelRes = R.string.exam_candidates_remark_type_absent_without_excuse,
        section = RemarkSection.Attendance,
        severity = RemarkSeverity.Info,
        icon = Icons.Filled.Inventory2,
    ),
    ProjectIncomplete(
        code = "PI",
        labelRes = R.string.exam_candidates_remark_type_project_incomplete,
        section = RemarkSection.Status,
        severity = RemarkSeverity.Info,
        icon = Icons.AutoMirrored.Filled.Notes,
    ),
    Sick(
        code = "S",
        labelRes = R.string.exam_candidates_remark_type_sick,
        section = RemarkSection.Status,
        severity = RemarkSeverity.Info,
        icon = Icons.Filled.CheckCircle,
    ),
    ExamsMalpractice(
        code = "EM",
        labelRes = R.string.exam_candidates_remark_type_exams_malpractice,
        section = RemarkSection.Conduct,
        severity = RemarkSeverity.Critical,
        icon = Icons.Filled.Error,
    ),
}

/** Drives the "Add Remark" modal. [Closed] renders nothing. */
sealed interface AddRemarkUiState {
    data object Closed : AddRemarkUiState
    data class Open(
        val candidateId: String,
        val candidateName: String,
        val selectedType: RemarkType? = null,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
    ) : AddRemarkUiState
}

internal fun RemarkSeverity.accentColor(): Color = when (this) {
    RemarkSeverity.Info -> PrimaryGreen
    RemarkSeverity.Warning -> WarningYellow
    RemarkSeverity.Critical -> ErrorRed
}

@Composable
fun AddRemarkDialog(
    state: AddRemarkUiState.Open,
    onSelectType: (RemarkType) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(20.dp),
            color = scheme.surface,
            shadowElevation = 16.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.exam_candidates_remark_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    IconButton(onClick = onDismiss, enabled = !state.isSaving) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.exam_candidates_remark_dialog_close),
                        )
                    }
                }
                HorizontalDivider(color = scheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = stringResource(R.string.exam_candidates_remark_dialog_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    RemarkSection.entries.forEach { section ->
                        val options = RemarkType.entries.filter { it.section == section }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(section.labelRes).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                            )
                            options.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowItems.forEach { type ->
                                        RemarkTypeCard(
                                            type = type,
                                            selected = state.selectedType == type,
                                            onClick = { onSelectType(type) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.error,
                        )
                    }
                }
                HorizontalDivider(color = scheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !state.isSaving) {
                        Text(stringResource(R.string.exam_candidates_remark_dialog_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = state.selectedType != null && !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = scheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.exam_candidates_remark_dialog_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun RemarkTypeCard(
    type: RemarkType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = type.severity.accentColor()
    Surface(
        onClick = onClick,
        modifier = modifier.testTag(type.code),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.06f) else scheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else scheme.outlineVariant,
        ),
        shadowElevation = 1.dp,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accent.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = type.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = if (selected) accent else scheme.outlineVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(type.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                )
            }
        }
    }
}
