package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton

/** Matches Hub assigned-institution cards and GridMenuCard chrome. */
private val ProfileCardBorder = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
private val ProfileNestedBorder = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
private val GradeBGreen = Color(0xFF1B6D24)

@Composable
fun InstitutionProfileContent(
    modifier: Modifier = Modifier,
    uiState: InstitutionProfileUiState,
    onBack: () -> Unit = {},
    onStartNewAssessment: (institutionId: String) -> Unit = {},
    onViewDocuments: (institutionId: String) -> Unit = {},
    onHistoryItemClick: (institutionId: String, historyId: String) -> Unit = { _, _ -> },
) {
    val success = uiState as? InstitutionProfileUiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProfileTopBar(onBack = onBack)
        },
        bottomBar = {
            if (success != null) {
                ProfileBottomActionBar(
                    onStartNewAssessment = {
                        onStartNewAssessment(success.institution.id)
                    },
                    onViewDocuments = { onViewDocuments(success.institution.id) },
                )
            }
        },
    ) { paddingValues ->
        when (uiState) {
            InstitutionProfileUiState.Loading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is InstitutionProfileUiState.NotFound -> NotFoundContent(
                modifier = Modifier.padding(paddingValues),
                institutionId = uiState.institutionId,
                onBack = onBack,
            )

            is InstitutionProfileUiState.Error -> ErrorContent(
                modifier = Modifier.padding(paddingValues),
                message = uiState.message,
                onBack = onBack,
            )

            is InstitutionProfileUiState.Success -> ProfileBody(
                modifier = Modifier.padding(paddingValues),
                institution = uiState.institution,
                onHistoryItemClick = { historyId ->
                    onHistoryItemClick(uiState.institution.id, historyId)
                },
            )
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.app_top_bar_back_action),
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.accreditation_profile_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.accreditation_profile_officer_avatar_cd),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileBottomActionBar(
    onStartNewAssessment: () -> Unit,
    onViewDocuments: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onViewDocuments,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = ProfileCardBorder,
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryGreen,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.accreditation_profile_action_view_documents),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = onStartNewAssessment,
                modifier = Modifier
                    .weight(1.25f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_profile_action_start_assessment),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileBody(
    institution: InstitutionProfileData,
    onHistoryItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DossierHeroCard(institution = institution)
        GeneralInfoCard(institution = institution)
        ProgrammesCard(institution = institution)
        CapacityCard(institution = institution)
        HistoryCard(
            history = institution.history,
            onHistoryItemClick = onHistoryItemClick,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Sync strip + photo + naming + facility/visit stats in one dossier card. */
@Composable
private fun DossierHeroCard(institution: InstitutionProfileData) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = ProfileCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileSyncBadge(status = institution.syncStatus)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = institution.lastSyncedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(
                color = scheme.surfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Box(
                modifier = Modifier.padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!institution.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(institution.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = institution.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Domain,
                            contentDescription = institution.name,
                            tint = PrimaryGreen.copy(alpha = 0.55f),
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryGreen.copy(alpha = 0.10f)),
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    color = PrimaryGreen,
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.accreditation_profile_verified_cd),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Text(
                text = institution.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
            )
            Text(
                text = institution.ownershipSubtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceVariant.copy(alpha = 0.55f),
                border = ProfileNestedBorder,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetaStat(
                        label = stringResource(R.string.accreditation_profile_facility_code),
                        value = institution.facilityCode,
                        valueColor = PrimaryGreen,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(scheme.outlineVariant),
                    )
                    MetaStat(
                        label = stringResource(R.string.accreditation_profile_last_visit),
                        value = institution.lastVisitLabel,
                        valueColor = scheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GeneralInfoCard(institution: InstitutionProfileData) {
    ProfileSectionCard(
        icon = Icons.Filled.LocationOn,
        title = stringResource(R.string.accreditation_profile_section_general),
        tintedHeader = true,
        contentPadding = false,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileDetailRow(
                label = stringResource(R.string.accreditation_profile_ownership),
                value = institution.ownershipType,
                showDivider = true,
            )
            ProfileDetailRow(
                label = stringResource(R.string.accreditation_profile_state),
                value = institution.state,
                showDivider = true,
            )
            ProfileDetailRow(
                label = stringResource(R.string.accreditation_profile_zone),
                value = institution.zone,
                showDivider = true,
            )
            ProfileDetailRow(
                label = stringResource(R.string.accreditation_profile_campus),
                value = institution.campusAddress,
                showDivider = false,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgrammesCard(institution: InstitutionProfileData) {
    ProfileSectionCard(
        icon = Icons.Filled.School,
        title = stringResource(R.string.accreditation_profile_section_programmes),
        trailing = {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = PrimaryGreen.copy(alpha = 0.14f),
            ) {
                Text(
                    text = stringResource(
                        R.string.accreditation_profile_programmes_count_format,
                        institution.programmesAccreditedCount,
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
        },
    ) {
        Text(
            text = stringResource(R.string.accreditation_profile_programmes_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            institution.programmes.forEach { programme ->
                ProgrammeChip(label = programme)
            }
        }
    }
}

@Composable
private fun ProgrammeChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = ProfileNestedBorder,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
            )
        }
    }
}

@Composable
private fun CapacityCard(institution: InstitutionProfileData) {
    ProfileSectionCard(
        icon = Icons.Filled.Groups,
        title = stringResource(R.string.accreditation_profile_section_capacity),
        trailing = {
            Text(
                text = stringResource(R.string.accreditation_profile_ssq_section_a),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CapacityStatTile(
                label = stringResource(R.string.accreditation_profile_staff_academic),
                value = institution.academicStaffCount.toString(),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                emphasize = true,
                modifier = Modifier.weight(1f),
            )
            CapacityStatTile(
                label = stringResource(R.string.accreditation_profile_staff_technical),
                value = institution.technicalStaffCount.toString(),
                icon = Icons.Filled.MedicalServices,
                emphasize = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CapacityStatTile(
                label = stringResource(R.string.accreditation_profile_staff_admin),
                value = institution.adminStaffCount.toString(),
                icon = Icons.Filled.Badge,
                emphasize = false,
                modifier = Modifier.weight(1f),
            )
            CapacityStatTile(
                label = stringResource(R.string.accreditation_profile_staff_ratio),
                value = institution.techToAcademicRatio,
                icon = Icons.Filled.Balance,
                emphasize = false,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            border = ProfileNestedBorder,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.accreditation_profile_capital_base),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = institution.capitalBaseLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Composable
private fun CapacityStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    emphasize: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.55f),
        border = ProfileNestedBorder,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (emphasize) {
                                SuccessGreen.copy(alpha = 0.18f)
                            } else {
                                scheme.surfaceVariant
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (emphasize) PrimaryGreen else scheme.onSurface,
            )
        }
    }
}

@Composable
private fun HistoryCard(
    history: List<AccreditationHistoryItemUiState>,
    onHistoryItemClick: (String) -> Unit,
) {
    ProfileSectionCard(
        icon = Icons.Filled.VerifiedUser,
        title = stringResource(R.string.accreditation_profile_section_history),
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDone,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.accreditation_profile_history_verified),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen,
                )
            }
        },
    ) {
        Text(
            text = stringResource(R.string.accreditation_profile_history_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            history.forEach { item ->
                HistoryRow(
                    item = item,
                    onClick = { onHistoryItemClick(item.id) },
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: AccreditationHistoryItemUiState,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val gradeColor = historyGradeColor(item.gradeLabel)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.55f),
        border = ProfileNestedBorder,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.cycleLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gradeColor,
                    ) {
                        Text(
                            text = item.gradeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.accreditation_profile_history_score_format,
                            item.scoreLabel,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen,
                    )
                }
                Text(
                    text = item.decisionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = SuccessGreen,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.accreditation_profile_history_open_cd),
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private fun historyGradeColor(gradeLabel: String): Color {
    val normalized = gradeLabel.trim().uppercase()
    return when {
        normalized.contains("GRADE A") || normalized == "A" -> SuccessGreen
        else -> GradeBGreen
    }
}

@Composable
private fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    tintedHeader: Boolean = false,
    contentPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = ProfileCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (tintedHeader) {
                            Modifier
                                .background(scheme.surfaceVariant.copy(alpha = 0.55f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        } else {
                            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                }
                trailing?.invoke()
            }
            if (tintedHeader) {
                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))
            } else {
                HorizontalDivider(
                    color = scheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            // Column (not Box): content lambdas emit multiple siblings that
            // must stack vertically — Box would overlay them as a watermark.
            Column(
                modifier = if (contentPadding) {
                    Modifier.padding(16.dp)
                } else {
                    Modifier.padding(bottom = 4.dp)
                },
            ) {
                content()
            }
        }
    }
}

/**
 * Label/value row mirroring RecordDetailScreen.DetailRow — kept local so
 * accreditation doesn't reach into verification's private composables.
 */
@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    showDivider: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ProfileSyncBadge(status: InstitutionSyncStatus) {
    val (labelRes, tint) = when (status) {
        InstitutionSyncStatus.Synced ->
            R.string.accreditation_hub_sync_synced to SuccessGreen
        InstitutionSyncStatus.DraftPendingSync ->
            R.string.accreditation_hub_sync_draft_pending to WarningYellow
        InstitutionSyncStatus.SyncFailed ->
            R.string.accreditation_hub_sync_failed to ErrorRed
        InstitutionSyncStatus.NeverSynced ->
            R.string.accreditation_hub_sync_never to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Text(
                text = stringResource(labelRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
            )
        }
    }
}

@Composable
private fun NotFoundContent(
    institutionId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.PersonSearch,
            contentDescription = null,
            tint = PrimaryGreen.copy(alpha = 0.55f),
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(R.string.accreditation_profile_not_found_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryGreen,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                R.string.accreditation_profile_not_found_subtitle_format,
                institutionId.ifBlank { "—" },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.accreditation_profile_action_back_to_hub),
            onClick = onBack,
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.accreditation_profile_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.accreditation_profile_action_back_to_hub),
            onClick = onBack,
        )
    }
}

@Preview(showBackground = true, widthDp = 411)
@Composable
private fun InstitutionProfileContentPreview() {
    ChprbnTheme {
        InstitutionProfileContent(uiState = InstitutionProfileUiState.placeholder())
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Not found")
@Composable
private fun InstitutionProfileNotFoundPreview() {
    ChprbnTheme {
        InstitutionProfileContent(
            uiState = InstitutionProfileUiState.NotFound("missing-id"),
        )
    }
}
