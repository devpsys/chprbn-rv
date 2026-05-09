package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

/** Warning shade for unscored items — matches existing app convention. */
private val UnscoredAccentColor = Color(0xFFEF6C00)

@Composable
fun AssessmentPracticalScoringContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentPracticalScoringUiState,
    onBack: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onIncrement: (questionId: String) -> Unit = {},
    onDecrement: (questionId: String) -> Unit = {},
    onSaveScores: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ScoringTopBar(onBack = onBack, onInfoClick = onInfoClick)
        },
        floatingActionButton = {
            SaveScoresFab(onClick = onSaveScores)
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                // Trailing space keeps the last question card scrollable
                // clear of the Extended FAB.
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SectionHeaderCard(title = uiState.sectionTitle) }
            items(items = uiState.questions, key = { it.id }) { question ->
                QuestionCard(
                    question = question,
                    onIncrement = { onIncrement(question.id) },
                    onDecrement = { onDecrement(question.id) },
                )
            }
        }
    }
}

@Composable
private fun ScoringTopBar(onBack: () -> Unit, onInfoClick: () -> Unit) {
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
                    contentDescription = stringResource(R.string.assessment_practical_scoring_action_back),
                    tint = scheme.onPrimary,
                )
            }
            Text(
                text = stringResource(R.string.assessment_practical_scoring_header_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionHeaderCard(title: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        // IntrinsicSize.Min lets the accent strip stretch to the text's
        // natural height without hard-coding a number.
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(scheme.primary),
            )
            Text(
                text = title.uppercase(),
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: ScoreQuestionUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val accent = if (question.isScored) scheme.primary else UnscoredAccentColor
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        // Strip + content; intrinsic min height lets the accent stripe
        // span the full card (including the controls row at the bottom).
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                // Question body — image, prompt, status row.
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!question.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(scheme.surfaceVariant),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(question.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(
                                    R.string.assessment_practical_scoring_question_image_cd_format,
                                    question.number,
                                ),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(
                                R.string.assessment_practical_scoring_question_number_format,
                                question.number,
                                question.prompt,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = if (question.isScored) {
                                    Icons.Filled.CheckCircle
                                } else {
                                    Icons.Outlined.PendingActions
                                },
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(
                                    if (question.isScored) {
                                        R.string.assessment_practical_scoring_status_scored
                                    } else {
                                        R.string.assessment_practical_scoring_status_unscored
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = accent,
                            )
                        }
                    }
                }
                // Divider between body and controls.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(scheme.outlineVariant),
                )
                // Controls row — slightly tinted bg.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(
                            R.string.assessment_practical_scoring_max_score_format,
                            question.maxScore,
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ScoreStepper(
                        question = question,
                        accent = accent,
                        onDecrement = onDecrement,
                        onIncrement = onIncrement,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreStepper(
    question: ScoreQuestionUiState,
    accent: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val canDecrement = question.score > 0
    val canIncrement = question.score < question.maxScore
    val scoreSummary = stringResource(
        R.string.assessment_practical_scoring_max_score_format,
        question.maxScore,
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = scheme.surface,
        border = BorderStroke(if (question.isScored) 1.dp else 1.5.dp, accent),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StepperButton(
                imageVector = Icons.Outlined.Remove,
                contentDescription = stringResource(
                    R.string.assessment_practical_scoring_decrement_cd_format,
                    question.number,
                ),
                enabled = canDecrement,
                onClick = onDecrement,
            )
            Text(
                text = question.score.toString(),
                modifier = Modifier
                    .width(28.dp)
                    .semantics { contentDescription = "${question.score} / $scoreSummary" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (question.isScored) scheme.onSurface else accent,
                textAlign = TextAlign.Center,
            )
            StepperButton(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(
                    R.string.assessment_practical_scoring_increment_cd_format,
                    question.number,
                ),
                enabled = canIncrement,
                onClick = onIncrement,
            )
        }
    }
}

@Composable
private fun StepperButton(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor = if (enabled) scheme.primary else scheme.surfaceVariant
    val tint = if (enabled) scheme.onPrimary else scheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        modifier = Modifier.size(32.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SaveScoresFab(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    ExtendedFloatingActionButton(
        onClick = onClick,
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
            text = stringResource(R.string.assessment_practical_scoring_action_save),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun AssessmentPracticalScoringContentPreview() {
    ChprbnTheme {
        AssessmentPracticalScoringContent(
            uiState = AssessmentPracticalScoringUiState(
                sectionTitle = "Section A — Vital Signs",
                questions = listOf(
                    ScoreQuestionUiState(
                        id = "q1",
                        number = 1,
                        prompt = "Measure blood pressure accurately.",
                        imageUrl = null,
                        maxScore = 10,
                        score = 10,
                    ),
                    ScoreQuestionUiState(
                        id = "q2",
                        number = 2,
                        prompt = "Demonstrate respiratory rate check.",
                        imageUrl = null,
                        maxScore = 10,
                        score = 0,
                    ),
                ),
            ),
        )
    }
}
