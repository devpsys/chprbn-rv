package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
internal fun SectionBFormFields(
    data: SsqSectionFormData,
    scrollState: ScrollState,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val subsectionOffsets = remember { mutableMapOf<String, Int>() }
    var selectedTabId by remember {
        mutableStateOf(data.checklistSubsections.firstOrNull()?.id.orEmpty())
    }

    SectionBHeaderCard(data = data)

    if (data.checklistSubsections.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                data.checklistSubsections.forEach { subsection ->
                    val selected = selectedTabId == subsection.id
                    Surface(
                        onClick = {
                            selectedTabId = subsection.id
                            val offset = subsectionOffsets[subsection.id] ?: return@Surface
                            scope.launch { scrollState.animateScrollTo(offset) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        },
                        shadowElevation = if (selected) 1.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (subsection.id == "B1") {
                                    Icons.AutoMirrored.Filled.MenuBook
                                } else {
                                    Icons.Filled.AccountBalance
                                },
                                contentDescription = null,
                                tint = if (selected) {
                                    PrimaryGreen
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = subsection.tabLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) {
                                    PrimaryGreen
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

    data.checklistSubsections.forEach { subsection ->
        val items = data.checklistItems.filter { it.subsectionId == subsection.id }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    subsectionOffsets[subsection.id] = coordinates.positionInParent().y.toInt()
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SubsectionGroupHeader(
                badge = subsection.badge,
                title = subsection.title,
                subtitle = subsection.subtitle,
                itemCount = items.size,
            )
            items.forEach { item ->
                SectionBChecklistItemCard(
                    item = item,
                    onChecklistAnswer = onChecklistAnswer,
                    onChecklistRemarks = onChecklistRemarks,
                )
            }
            AttachmentManagementCard(
                attachments = subsection.attachments,
                title = subsection.attachmentsTitle,
                hint = subsection.attachmentsHint,
                countLabel = stringResource(
                    R.string.accreditation_section_attachments_files_format,
                    subsection.attachments.size,
                ),
                photoLabel = stringResource(R.string.accreditation_section_take_photo),
                documentLabel = stringResource(R.string.accreditation_section_add_document),
                onAddPhoto = onAddPhoto,
                onAddDocument = onAddDocument,
                onRemoveAttachment = onRemoveAttachment,
                mutedBackground = true,
            )
        }
    }
}

@Composable
private fun SectionBHeaderCard(data: SsqSectionFormData) {
    val evaluated = data.evaluatedItemCount
    val total = data.totalChecklistCount.coerceAtLeast(1)
    val progress = evaluated.toFloat() / total.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssuredWorkload,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = data.formCodeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SectionStatusChip(status = data.status)
            }
            Text(
                text = stringResource(R.string.accreditation_section_b_heading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_section_completion_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.accreditation_section_evaluated_format,
                            evaluated,
                            data.totalChecklistCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = PrimaryGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun SubsectionGroupHeader(
    badge: String,
    title: String,
    subtitle: String,
    itemCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = stringResource(
                    R.string.accreditation_section_items_count_format,
                    itemCount,
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionBChecklistItemCard(
    item: SsqChecklistItemUiState,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
) {
    val (statusIcon, statusTint) = when (item.answer) {
        ChecklistAnswer.Yes, ChecklistAnswer.Available ->
            Icons.Filled.CheckCircle to SuccessGreen
        ChecklistAnswer.No, ChecklistAnswer.NotAvailable ->
            Icons.Filled.Cancel to ErrorRed
        ChecklistAnswer.Unanswered ->
            Icons.Outlined.RadioButtonUnchecked to MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.numberInSubsection.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (item.usesAvailability) {
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_available),
                            selected = item.answer == ChecklistAnswer.Available,
                            selectedColor = PrimaryGreen,
                            icon = Icons.Filled.Check,
                            onClick = {
                                onChecklistAnswer(item.id, ChecklistAnswer.Available)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_not_available),
                            selected = item.answer == ChecklistAnswer.NotAvailable,
                            selectedColor = ErrorRed,
                            icon = Icons.Filled.Close,
                            onClick = {
                                onChecklistAnswer(item.id, ChecklistAnswer.NotAvailable)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_compliant),
                            selected = item.answer == ChecklistAnswer.Yes,
                            selectedColor = PrimaryGreen,
                            icon = Icons.Filled.Check,
                            onClick = { onChecklistAnswer(item.id, ChecklistAnswer.Yes) },
                            modifier = Modifier.weight(1f),
                        )
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_non_compliant),
                            selected = item.answer == ChecklistAnswer.No,
                            selectedColor = ErrorRed,
                            icon = Icons.Filled.Close,
                            onClick = { onChecklistAnswer(item.id, ChecklistAnswer.No) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.accreditation_section_remarks_field),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = item.remarks,
                    onValueChange = { onChecklistRemarks(item.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.accreditation_section_remarks_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

