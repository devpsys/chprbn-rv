package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

private val AvatarSize = 128.dp

@Composable
fun AssessmentProjectAssessmentContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentProjectAssessmentUiState,
    onBack: () -> Unit = {},
    onScoreChange: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    onSaveScore: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ProjectAssessmentTopBar(onBack = onBack)
        },
        floatingActionButton = {
            ProjectAssessmentFabs(
                onCancel = onCancel,
                onSave = onSaveScore,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Centred body capped at ~480dp wide to mirror the design's
            // `max-w-md mx-auto` layout on tablets / wide phones.
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                CandidateProfile(uiState = uiState)
                Spacer(modifier = Modifier.height(40.dp))
                ScoreCard(
                    scoreText = uiState.scoreText,
                    maxScore = uiState.maxScore,
                    onScoreChange = onScoreChange,
                )
                // Bottom space large enough to clear both stacked FABs.
                Spacer(modifier = Modifier.height(160.dp))
            }
        }
    }
}

@Composable
private fun ProjectAssessmentTopBar(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.primary,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.assessment_project_action_back),
                    tint = scheme.onPrimary,
                )
            }
            Text(
                text = stringResource(R.string.assessment_project_header_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BrandChip() {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = scheme.onPrimary,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.assessment_project_brand_initials),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CandidateProfile(uiState: AssessmentProjectAssessmentUiState) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(AvatarSize)) {
            // Avatar — circular with white border + soft shadow.
            Surface(
                modifier = Modifier
                    .size(AvatarSize)
                    .border(4.dp, scheme.onPrimary, CircleShape),
                shape = CircleShape,
                color = scheme.surfaceVariant,
                shadowElevation = 4.dp,
            ) {
                if (!uiState.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(
                            R.string.assessment_project_avatar_cd_format,
                            uiState.candidateName,
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = stringResource(
                                R.string.assessment_project_avatar_cd_format,
                                uiState.candidateName,
                            ),
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }
            }
            // Verified badge — anchored to the avatar's bottom-right.
            if (uiState.verified) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .border(2.dp, scheme.onPrimary, CircleShape),
                    shape = CircleShape,
                    color = scheme.primary,
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = stringResource(R.string.assessment_project_verified_cd),
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = uiState.candidateName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uiState.examId,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(scheme.outlineVariant),
            )
            Text(
                text = uiState.role,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScoreCard(
    scoreText: String,
    maxScore: Int,
    onScoreChange: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.assessment_project_score_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            ScoreInput(
                scoreText = scoreText,
                maxScore = maxScore,
                onScoreChange = onScoreChange,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp),
                )
                Text(
                    text = stringResource(R.string.assessment_project_score_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScoreInput(
    scoreText: String,
    maxScore: Int,
    onScoreChange: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val centeredStyle = MaterialTheme.typography.displayMedium.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = scheme.primary,
    )
    OutlinedTextField(
        value = scoreText,
        onValueChange = onScoreChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        textStyle = centeredStyle,
        placeholder = {
            Text(
                text = stringResource(R.string.assessment_project_score_placeholder),
                modifier = Modifier.fillMaxWidth(),
                style = centeredStyle.copy(color = scheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
        },
        suffix = {
            Text(
                text = stringResource(R.string.assessment_project_score_suffix_format, maxScore),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = scheme.surface,
            unfocusedContainerColor = scheme.surface,
            focusedBorderColor = scheme.primary,
            unfocusedBorderColor = scheme.primary.copy(alpha = 0.2f),
            cursorColor = scheme.primary,
        ),
    )
}

@Composable
private fun ProjectAssessmentFabs(
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Cancel — smaller "secondary" pill above the primary action.
        ExtendedFloatingActionButton(
            onClick = onCancel,
            containerColor = scheme.surfaceVariant,
            contentColor = scheme.onSurfaceVariant,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.assessment_project_action_cancel),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        ExtendedFloatingActionButton(
            onClick = onSave,
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.assessment_project_action_save),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun AssessmentProjectAssessmentContentPreview() {
    ChprbnTheme {
        AssessmentProjectAssessmentContent(
            uiState = AssessmentProjectAssessmentUiState(
                candidateName = "Johnathan Doe",
                examId = "EX-2024-0092",
                role = "Clinical Practitioner",
                photoUrl = null,
                verified = true,
                scoreText = "",
                maxScore = 10,
            ),
        )
    }
}
