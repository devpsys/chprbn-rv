package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp as lerpFloat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

/** Warning shade for the "Incomplete" pill (mirrors the design's `secondary` token). */
private val IncompleteAccentColor = Color(0xFFEF6C00)

/** Capacity for the section cards' decorative top-right corner shape. */
private val CornerDecorationSize = 80.dp

/** Avatar displayed in the candidate summary card. */
private val AvatarSize = 72.dp

/** Enlarged-photo card target size — 3× the avatar. */
private val ExpandedPhotoSize = AvatarSize * 3

@Composable
fun AssessmentPracticalSectionsContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentPracticalSectionsUiState,
    onBack: () -> Unit = {},
    onSectionClick: (PracticalSectionUiState) -> Unit = {},
    onAssessProject: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    var isPhotoExpanded by remember { mutableStateOf(false) }
    var avatarCenter by remember { mutableStateOf<Offset?>(null) }
    val dismissPhoto = { isPhotoExpanded = false }

    BackHandler(enabled = isPhotoExpanded, onBack = dismissPhoto)

    // Single transition drives the entire morph — avatar fade-out, scrim,
    // photo card translation + scale + corner radius — so they stay
    // perfectly in sync and reverse cleanly on dismiss.
    val transition = updateTransition(targetState = isPhotoExpanded, label = "photoZoom")
    val photoProgress = transition.animateFloat(
        transitionSpec = {
            // Slightly slower opening (eye follows the morph) than closing.
            if (false isTransitioningTo true) {
                tween(durationMillis = 280, easing = FastOutSlowInEasing)
            } else {
                tween(durationMillis = 220, easing = FastOutLinearInEasing)
            }
        },
        label = "photoProgress",
    ) { expanded -> if (expanded) 1f else 0f }
    // True between the moment the user taps and the moment the close
    // animation finishes; false only when the overlay is fully idle.
    val isMorphActive = transition.currentState || transition.targetState

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = scheme.background,
            topBar = {
                PracticalSectionsTopBar(onBack = onBack)
            },
            floatingActionButton = {
                AssessProjectFab(onClick = onAssessProject)
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
                    // Trailing space sized to clear the Extended FAB so the
                    // last card scrolls fully into view above it.
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    CandidateSummaryCard(
                        uiState = uiState,
                        onPhotoClick = { isPhotoExpanded = true },
                        onAvatarPositioned = { avatarCenter = it },
                        avatarVisible = !isMorphActive,
                    )
                }
                items(items = uiState.sections, key = { it.id }) { section ->
                    SectionCard(
                        section = section,
                        onClick = { onSectionClick(section) },
                    )
                }
            }
        }

        ExpandedPhotoOverlay(
            active = isMorphActive,
            progress = photoProgress,
            avatarCenter = avatarCenter,
            photoUrl = uiState.candidatePhotoUrl,
            candidateName = uiState.candidateName,
            onDismiss = dismissPhoto,
        )
    }
}

@Composable
private fun PracticalSectionsTopBar(onBack: () -> Unit) {
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
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.assessment_practical_sections_action_back),
                    tint = scheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    // Stand-in for the design's `clinical_notes` icon, which
                    // isn't in this version of material-icons-extended.
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = stringResource(R.string.assessment_practical_sections_header_icon_cd),
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.assessment_practical_sections_header_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CandidateSummaryCard(
    uiState: AssessmentPracticalSectionsUiState,
    onPhotoClick: () -> Unit,
    onAvatarPositioned: (Offset) -> Unit,
    avatarVisible: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    // Clip first so the ripple stays inside the rounded
                    // shape; clickable last so the indication overlays the
                    // background + border. Reports its window-space centre
                    // to the parent so the morph overlay knows where to
                    // fly from / back to.
                    modifier = Modifier
                        .size(AvatarSize)
                        .clip(RoundedCornerShape(16.dp))
                        .background(scheme.surfaceVariant)
                        .border(2.dp, scheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .clickable(
                            enabled = avatarVisible,
                            onClickLabel = stringResource(
                                R.string.assessment_practical_sections_photo_open_cd,
                            ),
                            onClick = onPhotoClick,
                        )
                        .onGloballyPositioned { coords ->
                            onAvatarPositioned(coords.boundsInWindow().center)
                        }
                        // Hide while the morph owns the photo so we don't
                        // see two copies during the flight; visible only
                        // when the transition is fully idle-closed.
                        .graphicsLayer { alpha = if (avatarVisible) 1f else 0f },
                    contentAlignment = Alignment.Center,
                ) {
                    if (!uiState.candidatePhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uiState.candidatePhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(
                                R.string.assessment_practical_sections_avatar_cd_format,
                                uiState.candidateName,
                            ),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = stringResource(
                                R.string.assessment_practical_sections_avatar_cd_format,
                                uiState.candidateName,
                            ),
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = uiState.candidateName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = scheme.primary.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = uiState.candidateExamId,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary,
                            )
                        }
                        Text(
                            text = stringResource(R.string.assessment_practical_sections_candidate_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(scheme.outlineVariant),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatColumn(
                    value = stringResource(
                        R.string.assessment_practical_sections_stat_done_format,
                        uiState.sectionsDone,
                        uiState.sectionsTotal,
                    ),
                    label = stringResource(R.string.assessment_practical_sections_stat_done_label),
                    valueColor = scheme.primary,
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(scheme.outlineVariant),
                )
                StatColumn(
                    value = uiState.sectionsRemaining.toString(),
                    label = stringResource(R.string.assessment_practical_sections_stat_remaining_label),
                    valueColor = IncompleteAccentColor,
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    valueColor: Color,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionCard(
    section: PracticalSectionUiState,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accentColor = when (section.status) {
        PracticalSectionStatus.Complete -> scheme.primary
        PracticalSectionStatus.Incomplete -> IncompleteAccentColor
        // Neutral grey for "Not Started" — design uses a tertiary red,
        // but in this app's palette red signals errors/failures, not a
        // not-yet-started state. Grey reads as a passive prompt.
        PracticalSectionStatus.NotStarted -> scheme.onSurfaceVariant
    }
    val statusIcon: ImageVector = when (section.status) {
        PracticalSectionStatus.Complete -> Icons.Filled.CheckCircle
        PracticalSectionStatus.Incomplete -> Icons.Outlined.PendingActions
        PracticalSectionStatus.NotStarted -> Icons.Outlined.School
    }
    val statusLabel = stringResource(
        when (section.status) {
            PracticalSectionStatus.Complete -> R.string.assessment_practical_sections_status_complete
            PracticalSectionStatus.Incomplete -> R.string.assessment_practical_sections_status_incomplete
            PracticalSectionStatus.NotStarted -> R.string.assessment_practical_sections_status_not_started
        }
    )
    val footerText = when (section.status) {
        PracticalSectionStatus.Complete -> stringResource(
            R.string.assessment_practical_sections_complete_updated_format,
            section.footerText,
        )
        PracticalSectionStatus.Incomplete -> stringResource(
            R.string.assessment_practical_sections_incomplete_remaining_format,
            section.footerText.toIntOrNull() ?: 0,
        )
        PracticalSectionStatus.NotStarted -> stringResource(
            R.string.assessment_practical_sections_not_started_caption,
        )
    }
    Surface(
        // Surface(onClick) keeps the ripple inside the rounded card.
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Box {
            // Decorative top-right corner shape with a tinted status icon.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(CornerDecorationSize)
                    .background(
                        color = accentColor.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(bottomStart = CornerDecorationSize),
                    ),
                contentAlignment = Alignment.TopEnd,
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp),
                )
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
                Text(
                    text = section.sectionTitle.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = section.sectionSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(scheme.outlineVariant.copy(alpha = 0.4f)),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = footerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (section.status == PracticalSectionStatus.Incomplete) {
                            accentColor
                        } else {
                            scheme.onSurfaceVariant
                        },
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.assessment_practical_sections_card_arrow_cd),
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/**
 * Tap-to-zoom overlay that morphs the candidate photo from the avatar's
 * exact on-screen position to the centre of the screen — combining
 * translation, scale, and corner-radius animation.
 *
 * Driven by a single [progress] State (0f = collapsed at avatar position,
 * 1f = expanded at screen centre). The translation is computed from the
 * delta between the avatar's window-space centre and the overlay's own
 * centre, so the photo's apparent centre tracks the avatar exactly at
 * progress 0 regardless of insets, status bar height, or screen size.
 *
 * Reads of [progress] are deferred to draw / layer phase via
 * `graphicsLayer { ... }` and `drawBehind { ... }` blocks so the per-frame
 * animation does not recompose this composable's parent.
 */
@Composable
private fun ExpandedPhotoOverlay(
    active: Boolean,
    progress: State<Float>,
    avatarCenter: Offset?,
    photoUrl: String?,
    candidateName: String,
    onDismiss: () -> Unit,
) {
    if (!active) return

    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dismissLabel = stringResource(R.string.assessment_practical_sections_photo_close_cd)
    val photoCd = stringResource(
        R.string.assessment_practical_sections_avatar_cd_format,
        candidateName,
    )

    var overlayCenter by remember { mutableStateOf<Offset?>(null) }

    // The avatar's collapsed-state corner radius lives at 16.dp visible.
    // To make the morph corner-seamless, the unscaled card radius is
    // computed so it produces the avatar's visible radius once the
    // collapsed scale is applied. (visibleRadius = unscaled * scale)
    val collapsedScale = AvatarSize.value / ExpandedPhotoSize.value // 0.333…
    val collapsedUnscaledCornerDp = 16f / collapsedScale            // 48dp
    val expandedCornerDp = 24f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayCenter = it.boundsInWindow().center }
            // Tap anywhere — scrim or photo — dismisses. `indication =
            // null` suppresses the ripple flash since this is a dismiss
            // gesture, not a button press.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = dismissLabel,
                onClick = onDismiss,
            )
            // Animated scrim painted in the draw phase so per-frame
            // alpha changes don't trigger recomposition.
            .drawBehind {
                drawRect(Color.Black.copy(alpha = 0.7f * progress.value.coerceIn(0f, 1f)))
            },
        contentAlignment = Alignment.Center,
    ) {
        // We can only render the photo card once we know both endpoints
        // of the morph (avatar position + overlay centre). On the first
        // frame after `active` flips true these may be null for one
        // layout pass; the scrim is essentially transparent then anyway.
        val avatar = avatarCenter
        val centre = overlayCenter
        if (avatar != null && centre != null) {
            val deltaX = avatar.x - centre.x
            val deltaY = avatar.y - centre.y
            Surface(
                modifier = Modifier
                    .size(ExpandedPhotoSize)
                    .graphicsLayer {
                        val p = progress.value.coerceIn(0f, 1f)
                        val invP = 1f - p
                        val scale = lerpFloat(collapsedScale, 1f, p)
                        scaleX = scale
                        scaleY = scale
                        translationX = deltaX * invP
                        translationY = deltaY * invP
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                        alpha = 1f
                        // Lerp unscaled corner radius so the visible
                        // radius (= unscaled * scale) morphs from the
                        // avatar's 16dp to the expanded card's 24dp.
                        val cornerDp = lerpFloat(collapsedUnscaledCornerDp, expandedCornerDp, p)
                        shape = RoundedCornerShape(cornerDp.dp)
                        clip = true
                        // Drop-shadow grows with the morph — no shadow at
                        // the avatar position (matches the flat avatar)
                        // up to a soft 16dp lift at full size.
                        shadowElevation = lerpFloat(0f, 16f, p) * density
                    },
                // graphicsLayer.shape handles rounding; keep the Surface
                // square so it doesn't double-round.
                shape = RectangleShape,
                color = scheme.surface,
                shadowElevation = 0.dp,
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = photoCd,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = photoCd,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(120.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessProjectFab(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = scheme.primary,
        contentColor = scheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
    ) {
        Icon(
            // Stand-in for the design's `assignment` icon.
            imageVector = Icons.AutoMirrored.Outlined.Assignment,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.assessment_practical_sections_action_assess_project),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun AssessmentPracticalSectionsContentPreview() {
    ChprbnTheme {
        AssessmentPracticalSectionsContent(
            uiState = AssessmentPracticalSectionsUiState(
                candidateName = "Jane Doe",
                candidateExamId = "EXAM-2024-001",
                candidatePhotoUrl = null,
                sectionsDone = 1,
                sectionsTotal = 3,
                sectionsRemaining = 2,
                sections = listOf(
                    PracticalSectionUiState(
                        id = "A",
                        sectionTitle = "Section A",
                        sectionSubtitle = "Patient Assessment",
                        status = PracticalSectionStatus.Complete,
                        footerText = "09:45 AM",
                    ),
                    PracticalSectionUiState(
                        id = "B",
                        sectionTitle = "Section B",
                        sectionSubtitle = "Clinical Diagnosis",
                        status = PracticalSectionStatus.Incomplete,
                        footerText = "2",
                    ),
                    PracticalSectionUiState(
                        id = "C",
                        sectionTitle = "Section C",
                        sectionSubtitle = "Ethical Standards",
                        status = PracticalSectionStatus.NotStarted,
                        footerText = "",
                    ),
                ),
            ),
        )
    }
}
